package com.homewifimanager.app.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class NetworkChangeMonitor(
    context: Context,
    private val onWifiStateChanged: (Boolean) -> Unit
) {

    private val connectivityManager =
        context.applicationContext.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    private var registered = false

    private val wifiNetworks = mutableSetOf<Network>()

    private val networkCallback =
        object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                updateNetwork(network)
            }

            override fun onLost(network: Network) {
                synchronized(wifiNetworks) {
                    wifiNetworks.remove(network)
                }

                refreshWifiNetworks()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                updateNetwork(
                    network,
                    networkCapabilities
                )
            }
        }

    private fun updateNetwork(
        network: Network,
        capabilities: NetworkCapabilities? =
            connectivityManager.getNetworkCapabilities(network)
    ) {

        val isWifi =
            capabilities?.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            ) == true

        synchronized(wifiNetworks) {

            if (isWifi) {
                wifiNetworks.add(network)
            } else {
                wifiNetworks.remove(network)
            }
        }

        notifyCurrentState()
    }

    private fun refreshWifiNetworks() {

        val currentWifiNetworks =
            mutableSetOf<Network>()

        for (network in connectivityManager.allNetworks) {

            val capabilities =
                connectivityManager.getNetworkCapabilities(network)

            if (
                capabilities?.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                ) == true
            ) {
                currentWifiNetworks.add(network)
            }
        }

        synchronized(wifiNetworks) {

            wifiNetworks.clear()
            wifiNetworks.addAll(currentWifiNetworks)
        }

        notifyCurrentState()
    }

    private fun notifyCurrentState() {

        val connected =
            synchronized(wifiNetworks) {
                wifiNetworks.isNotEmpty()
            }

        onWifiStateChanged(connected)
    }

    fun start() {

        if (registered) {
            refreshWifiNetworks()
            return
        }

        val request =
            NetworkRequest.Builder()
                .addTransportType(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
                .addCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                )
                .build()

        try {

            connectivityManager.registerNetworkCallback(
                request,
                networkCallback
            )

            registered = true

            // Immediately synchronize with Android's
            // current network state.
            refreshWifiNetworks()

        } catch (_: Exception) {

            registered = false

            synchronized(wifiNetworks) {
                wifiNetworks.clear()
            }

            notifyCurrentState()
        }
    }

    fun stop() {

        if (registered) {

            try {
                connectivityManager.unregisterNetworkCallback(
                    networkCallback
                )
            } catch (_: Exception) {
                // Already unregistered.
            }
        }

        synchronized(wifiNetworks) {
            wifiNetworks.clear()
        }

        registered = false

        notifyCurrentState()
    }
}