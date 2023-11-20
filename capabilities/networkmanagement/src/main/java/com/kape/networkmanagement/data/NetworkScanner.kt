package com.kape.networkmanagement.data

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import com.kape.networkmanagement.domain.NetworkInfoSource

class NetworkScanner(private val networkInfoSource: NetworkInfoSource) {
    val currentSSID = mutableStateOf<String?>(null)

    private val callback: ConnectivityManager.NetworkCallback =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    currentSSID.value = networkInfoSource.getSSID(networkCapabilities)
                    networkInfoSource.unregisterCallback(this)
                }
            }
        } else {
            object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    currentSSID.value = networkInfoSource.getSSID(networkCapabilities)
                    networkInfoSource.unregisterCallback(this)
                }
            }
        }

    fun scanNetwork() = networkInfoSource.registerCallback(callback)
}