package com.homewifimanager.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.homewifimanager.app.R
import com.homewifimanager.app.data.AppPreferences
import com.homewifimanager.app.network.NetworkChangeMonitor
import com.homewifimanager.app.network.WifiNetworkManager
import com.homewifimanager.app.usage.NetworkUsageManager
import com.homewifimanager.app.usage.UsageTimeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime

class UsageMonitorService : Service() {

    companion object {
        const val CHANNEL_ID = "usage_monitor_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START =
            "com.homewifimanager.app.action.START_MONITORING"

        const val ACTION_STOP =
            "com.homewifimanager.app.action.STOP_MONITORING"
    }

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var preferences: AppPreferences
    private lateinit var wifiManager: WifiNetworkManager
    private lateinit var networkUsageManager: NetworkUsageManager
    private lateinit var networkChangeMonitor: NetworkChangeMonitor

    @Volatile
    private var wifiConnected = false

    private val usageUpdateMutex = Mutex()

    private var monitorJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        preferences = AppPreferences(applicationContext)
        wifiManager = WifiNetworkManager(applicationContext)
        networkUsageManager = NetworkUsageManager(applicationContext)

        networkChangeMonitor =
            NetworkChangeMonitor(applicationContext) { connected ->

                wifiConnected = connected

                Log.d(
                    "UsageMonitorService",
                    "Wi-Fi connectivity changed: connected=$connected"
                )

                serviceScope.launch {
                    try {

                        /*
                         * If Wi-Fi is completely disconnected,
                         * the device cannot currently be connected
                         * to the selected Home Wi-Fi.
                         */
                        if (!connected) {
                            preferences.setConnectedToHomeNetwork(false)
                        }

                        updateUsage()

                    } catch (e: Exception) {
                        Log.e(
                            "UsageMonitorService",
                            "Network change update failed",
                            e
                        )
                    }
                }
            }

        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {

            ACTION_STOP -> {

                networkChangeMonitor.stop()
                monitorJob?.cancel()

                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()

                return START_NOT_STICKY
            }

            ACTION_START,
            null -> {

                startAsForegroundService()
                startMonitoring()
            }
        }

        return START_STICKY
    }

    private fun startAsForegroundService() {

        Log.d(
            "UsageMonitorService",
            "Starting foreground service"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            startForeground(
                NOTIFICATION_ID,
                createNotification(
                    "Starting usage monitoring…"
                ),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )

        } else {

            startForeground(
                NOTIFICATION_ID,
                createNotification(
                    "Starting usage monitoring…"
                )
            )
        }

        Log.d(
            "UsageMonitorService",
            "Foreground service started"
        )
    }

    private fun startMonitoring() {

        // Prevent multiple monitoring loops.
        if (monitorJob?.isActive == true) {
            return
        }

        networkChangeMonitor.start()

        monitorJob =
            serviceScope.launch {

                while (isActive) {

                    try {

                        updateUsage()

                    } catch (e: Exception) {

                        Log.e(
                            "UsageMonitorService",
                            "Unable to update usage",
                            e
                        )
                    }

                    delay(30_000L)
                }
            }
    }

    /*
     * Both the 30-second loop and NetworkChangeMonitor
     * can request an update.
     *
     * Mutex prevents two calculations from running
     * at the same time and double-counting an interval.
     */
    private suspend fun updateUsage() {

        usageUpdateMutex.withLock {
            performUsageUpdate()
        }
    }

    private suspend fun performUsageUpdate() {

        var settings =
            preferences.settings.first()

        val selectedSsid =
            settings.homeNetwork

        val dailyLimit =
            settings.dailyAllowanceBytes

        /*
         * Monitoring cannot start until onboarding
         * has saved both the Home Wi-Fi and allowance.
         */
        if (
            selectedSsid.isBlank() ||
            dailyLimit <= 0L
        ) {

            updateNotification(
                "Waiting for Home Wi-Fi setup"
            )

            return
        }

        val now =
            LocalDateTime.now()

        val nowMillis =
            System.currentTimeMillis()

        val today =
            UsageTimeManager.usageDate(now)

        val todayString =
            today.toString()

        /*
         * NEW DAY
         *
         * Reset the counted allowance and measurement
         * baseline when UsageTimeManager reports a new
         * allowance day.
         */
        if (settings.usageDate != todayString) {

            preferences.resetDailyUsage(
                todayString
            )

            settings =
                preferences.settings.first()
        }

        /*
         * FREE PERIOD
         *
         * 12:00 AM - 8:00 AM traffic is not counted.
         *
         * We still move the measurement timestamp forward
         * so free-period traffic can never be included in
         * the first measurement after 8 AM.
         */
        if (UsageTimeManager.isFreePeriod(now)) {

            preferences.setUsageTrackingState(
                countedBytes =
                    settings.countedUsageBytes,
                date =
                    todayString,
                snapshotBytes =
                    0L,
                snapshotTimeMillis =
                    nowMillis
            )

            updateNotification(
                "Free usage period • $selectedSsid"
            )

            return
        }

        /*
         * TRY TO IDENTIFY THE CURRENT SSID.
         *
         * On the Xiaomi/HyperOS device being tested,
         * Android can return null here after the normal
         * app UI is removed from Recents.
         */
        val currentSsid = wifiManager.getCurrentSsid()

        val connectedToHome =
            currentSsid?.trim()?.equals(
                selectedSsid.trim(),
                ignoreCase = true
            ) == true

        preferences.setConnectedToHomeNetwork(
            connectedToHome
        )

        /*
         * FIRST MEASUREMENT
         *
         * There is no previous timestamp yet.
         * Establish the baseline without adding old traffic.
         */
        if (
            settings.lastSnapshotTimeMillis <= 0L
        ) {

            preferences.setUsageTrackingState(
                countedBytes =
                    settings.countedUsageBytes,
                date =
                    todayString,
                snapshotBytes =
                    0L,
                snapshotTimeMillis =
                    nowMillis
            )

            updateNotification(
                if (connectedToHome) {

                    "${formatBytes(settings.countedUsageBytes)} used • Monitoring"

                } else {

                    "Waiting for $selectedSsid"
                }
            )

            return
        }

        /*
         * NOT ON HOME WI-FI
         *
         * Move the timestamp forward WITHOUT adding
         * any usage.
         *
         * This prevents mobile data / another Wi-Fi
         * interval from being added after reconnecting.
         */
        if (!connectedToHome) {

            preferences.setUsageTrackingState(
                countedBytes =
                    settings.countedUsageBytes,
                date =
                    todayString,
                snapshotBytes =
                    0L,
                snapshotTimeMillis =
                    nowMillis
            )

            updateNotification(
                "Waiting for $selectedSsid"
            )

            return
        }

        /*
         * HOME WI-FI IS ACTIVE
         *
         * Query only the Wi-Fi traffic between the
         * previous measurement and now.
         */
        val intervalStart =
            settings.lastSnapshotTimeMillis

        val intervalEnd =
            nowMillis

        val intervalBytes =
            if (intervalEnd > intervalStart) {

                networkUsageManager
                    .getWifiUsageBytes(
                        intervalStart,
                        intervalEnd
                    )
                    .coerceAtLeast(0L)

            } else {

                0L
            }

        val newCountedUsage =
            settings.countedUsageBytes +
                    intervalBytes

        /*
         * Persist both:
         *
         * 1. Today's accumulated usage.
         * 2. Timestamp for the next measurement.
         */
        preferences.setUsageTrackingState(
            countedBytes =
                newCountedUsage,
            date =
                todayString,
            snapshotBytes =
                0L,
            snapshotTimeMillis =
                nowMillis
        )

        val percentage =
            (
                    newCountedUsage.toDouble() /
                            dailyLimit.toDouble() *
                            100.0
                    )
                .coerceIn(
                    0.0,
                    100.0
                )

        Log.d(
            "UsageMonitorService",
            "SSID=$selectedSsid " +
                    "interval=$intervalBytes " +
                    "total=$newCountedUsage " +
                    "limit=$dailyLimit"
        )

        /*
         * LIMIT CHECK
         */
        if (newCountedUsage >= dailyLimit) {

            updateNotification(
                "Daily allowance reached"
            )

            /*
             * Limit-screen enforcement will be
             * connected here later.
             */

        } else {

            updateNotification(
                "${formatBytes(newCountedUsage)} used • " +
                        "${percentage.toInt()}%"
            )
        }
    }

    private fun updateNotification(
        message: String
    ) {

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        manager.notify(
            NOTIFICATION_ID,
            createNotification(message)
        )
    }

    private fun formatBytes(
        bytes: Long
    ): String {

        val mb =
            bytes /
                    (1024.0 * 1024.0)

        val gb =
            bytes /
                    (1024.0 * 1024.0 * 1024.0)

        return if (gb >= 1.0) {

            String.format(
                "%.2f GB",
                gb
            )

        } else {

            String.format(
                "%.0f MB",
                mb
            )
        }
    }

    private fun createNotificationChannel() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Wi-Fi usage monitoring",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {

                    description =
                        "Keeps Home WiFi Manager monitoring usage in the background."
                }

            val notificationManager =
                getSystemService(
                    NotificationManager::class.java
                )

            notificationManager
                .createNotificationChannel(
                    channel
                )
        }
    }

    private fun createNotification(
        message: String
    ) =
        NotificationCompat
            .Builder(
                this,
                CHANNEL_ID
            )
            .setSmallIcon(
                R.mipmap.ic_launcher
            )
            .setContentTitle(
                "Home WiFi Manager"
            )
            .setContentText(
                message
            )
            .setPriority(
                NotificationCompat.PRIORITY_LOW
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    override fun onDestroy() {

        networkChangeMonitor.stop()

        monitorJob?.cancel()

        serviceScope.cancel()

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }
}