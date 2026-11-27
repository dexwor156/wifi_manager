package com.homewifimanager.app.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager

class WifiNetworkManager(
    context: Context
) {

    private val appContext = context.applicationContext

    private val connectivityManager =
        appContext.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    private val wifiManager =
        appContext.getSystemService(
            Context.WIFI_SERVICE
        ) as WifiManager

    /**
     * Returns true only when the device's ACTIVE network
     * is Wi-Fi.
     */
    fun isConnectedToWifi(): Boolean {

        val activeNetwork =
            connectivityManager.activeNetwork
                ?: return false

        val capabilities =
            connectivityManager.getNetworkCapabilities(
                activeNetwork
            ) ?: return false

        return capabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_WIFI
        )
    }

    /**
     * Returns the currently connected Wi-Fi SSID.
     *
     * Returns null when:
     * - Wi-Fi is not the active network
     * - Android does not currently expose the SSID
     * - the SSID is unknown
     */
    @SuppressLint("MissingPermission")
    fun getCurrentSsid(): String? {

        if (!isConnectedToWifi()) {
            return null
        }

        val rawSsid =
            wifiManager.connectionInfo?.ssid

        if (
            rawSsid.isNullOrBlank() ||
            rawSsid == WifiManager.UNKNOWN_SSID ||
            rawSsid == "<unknown ssid>"
        ) {
            return null
        }

        return rawSsid
            .removeSurrounding("\"")
            .trim()
            .takeIf { it.isNotBlank() }
    }

    /**
     * Returns true only when the currently available
     * SSID positively matches the selected home network.
     */
    fun isConnectedToSelectedNetwork(
        selectedSsid: String
    ): Boolean {

        if (selectedSsid.isBlank()) {
            return false
        }

        val currentSsid =
            getCurrentSsid()
                ?: return false

        return currentSsid.equals(
            selectedSsid.trim(),
            ignoreCase = true
        )
    }
}