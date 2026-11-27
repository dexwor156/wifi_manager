package com.homewifimanager.app.usage

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager

class NetworkUsageManager(
    context: Context
) {

    private val networkStatsManager =
        context.getSystemService(Context.NETWORK_STATS_SERVICE)
                as NetworkStatsManager

    /**
     * Returns total Wi-Fi traffic used by the device
     * between startTimeMillis and endTimeMillis.
     *
     * Result includes both downloaded and uploaded bytes.
     */
    fun getWifiUsageBytes(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): Long {

        var totalBytes = 0L

        val stats = networkStatsManager.querySummary(
            ConnectivityManager.TYPE_WIFI,
            null,
            startTimeMillis,
            endTimeMillis
        )

        val bucket = NetworkStats.Bucket()

        try {
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)

                totalBytes += bucket.rxBytes
                totalBytes += bucket.txBytes
            }
        } finally {
            stats.close()
        }

        return totalBytes
    }
}