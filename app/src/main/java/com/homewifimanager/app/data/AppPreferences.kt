package com.homewifimanager.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "home_wifi_manager_preferences"
)

data class AppSettings(
    val connectedToHomeNetwork: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val monitoringEnabled: Boolean = false,
    val homeNetwork: String = "",
    val dailyAllowanceBytes: Long = 0L,
    val deviceId: String = "",
    val countedUsageBytes: Long = 0L,
    val usageDate: String = "",
    val lastWifiSnapshotBytes: Long = 0L,
    val lastSnapshotTimeMillis: Long = 0L,

)

class AppPreferences(
    private val context: Context
) {

    private object Keys {
        val ONBOARDING_COMPLETED =
            booleanPreferencesKey("onboarding_completed")

        val MONITORING_ENABLED =
            booleanPreferencesKey("monitoring_enabled")

        val HOME_NETWORK =
            stringPreferencesKey("home_network")

        val DAILY_ALLOWANCE_BYTES =
            longPreferencesKey("daily_allowance_bytes")

        val DEVICE_ID =
            stringPreferencesKey("device_id")

        val COUNTED_USAGE_BYTES =
            longPreferencesKey("counted_usage_bytes")

        val CONNECTED_TO_HOME_NETWORK =
            booleanPreferencesKey("connected_to_home_network")

        val USAGE_DATE =
            stringPreferencesKey("usage_date")

        val LAST_WIFI_SNAPSHOT_BYTES =
            longPreferencesKey("last_wifi_snapshot_bytes")

        val LAST_SNAPSHOT_TIME_MILLIS =
            longPreferencesKey("last_snapshot_time_millis")
    }

    val settings: Flow<AppSettings> =
        context.dataStore.data.map { preferences ->

            AppSettings(
                onboardingCompleted =
                    preferences[Keys.ONBOARDING_COMPLETED] ?: false,

                monitoringEnabled =
                    preferences[Keys.MONITORING_ENABLED] ?: false,

                homeNetwork =
                    preferences[Keys.HOME_NETWORK] ?: "",

                dailyAllowanceBytes =
                    preferences[Keys.DAILY_ALLOWANCE_BYTES] ?: 0L,

                connectedToHomeNetwork =
                    preferences[Keys.CONNECTED_TO_HOME_NETWORK] ?: false,

                deviceId =
                    preferences[Keys.DEVICE_ID] ?: "",

                countedUsageBytes =
                    preferences[Keys.COUNTED_USAGE_BYTES] ?: 0L,
                lastWifiSnapshotBytes =
                    preferences[Keys.LAST_WIFI_SNAPSHOT_BYTES] ?: 0L,

                lastSnapshotTimeMillis =
                    preferences[Keys.LAST_SNAPSHOT_TIME_MILLIS] ?: 0L,

                usageDate =
                    preferences[Keys.USAGE_DATE] ?: "",
            )
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setMonitoringEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.MONITORING_ENABLED] = enabled
        }
    }

    suspend fun setHomeNetwork(network: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.HOME_NETWORK] = network
        }
    }

    suspend fun setDailyAllowanceBytes(bytes: Long) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DAILY_ALLOWANCE_BYTES] = bytes
        }
    }

    suspend fun setDeviceId(deviceId: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DEVICE_ID] = deviceId
        }
    }

    suspend fun setCountedUsage(
        bytes: Long,
        date: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[Keys.COUNTED_USAGE_BYTES] = bytes
            preferences[Keys.USAGE_DATE] = date
        }
    }
    suspend fun setUsageTrackingState(
        countedBytes: Long,
        date: String,
        snapshotBytes: Long,
        snapshotTimeMillis: Long
    ) {
        context.dataStore.edit { preferences ->

            preferences[Keys.COUNTED_USAGE_BYTES] =
                countedBytes

            preferences[Keys.USAGE_DATE] =
                date

            preferences[Keys.LAST_WIFI_SNAPSHOT_BYTES] =
                snapshotBytes

            preferences[Keys.LAST_SNAPSHOT_TIME_MILLIS] =
                snapshotTimeMillis
        }
    }

    suspend fun resetDailyUsage(date: String) {
        context.dataStore.edit { preferences ->

            preferences[Keys.COUNTED_USAGE_BYTES] = 0L
            preferences[Keys.USAGE_DATE] = date

            preferences[Keys.LAST_WIFI_SNAPSHOT_BYTES] = 0L
            preferences[Keys.LAST_SNAPSHOT_TIME_MILLIS] = 0L
        }
    }
    suspend fun setConnectedToHomeNetwork(
        connected: Boolean
    ) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CONNECTED_TO_HOME_NETWORK] = connected
        }
    }
}