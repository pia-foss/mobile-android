package com.kape.networkmanagement.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.kape.networkmanagement.domain.NetworkInfoSource

class NetworkInfoSourceImpl(
    private val connectivityManager: ConnectivityManager,
    private val wifiManager: WifiManager,
) : NetworkInfoSource {

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    override fun registerDefaultCallback(
        callback: ConnectivityManager.NetworkCallback,
        context: Context,
        receiver: BroadcastReceiver,
    ) {
        connectivityManager.registerDefaultNetworkCallback(callback)
        context.registerReceiver(receiver, IntentFilter())
    }

    override fun registerCallback(callback: ConnectivityManager.NetworkCallback) {
        connectivityManager.registerNetworkCallback(networkRequest, callback)
    }

    override fun unregisterCallback(callback: ConnectivityManager.NetworkCallback) {
        connectivityManager.unregisterNetworkCallback(callback)
    }

    override fun triggerUpdate(callback: ConnectivityManager.NetworkCallback) {
        connectivityManager.requestNetwork(networkRequest, callback)
    }

    override fun getSSID(networkCapabilities: NetworkCapabilities): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            (networkCapabilities.transportInfo as WifiInfo).ssid
        } else {
            wifiManager.connectionInfo.ssid
        }
    }
}