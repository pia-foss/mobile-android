package com.kape.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NetworkConnectionListener(
    context: Context,
    private val networkUpdateHandler: (ssid: String, isWifi: Boolean) -> Unit,
    receiver: BroadcastReceiver,
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val defaultCallback =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onAvailable(network: Network) {
                    _isConnected.value = true
                }

                override fun onLost(network: Network) {
                    _isConnected.value =
                        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    if (networkCapabilities.transportInfo is WifiInfo) {
                        _ssid.value = getSSID(networkCapabilities)
                        networkUpdateHandler(
                            getSSID(networkCapabilities),
                            networkCapabilities.hasTransport(
                                NetworkCapabilities.TRANSPORT_WIFI,
                            ),
                        )
                    } else {
                        networkUpdateHandler(
                            "",
                            networkCapabilities.hasTransport(
                                NetworkCapabilities.TRANSPORT_WIFI,
                            ),
                        )
                    }
                }
            }
        } else {
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isConnected.value = true
                }

                override fun onLost(network: Network) {
                    _isConnected.value =
                        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    _ssid.value = getSSID(networkCapabilities)
                    networkUpdateHandler(
                        getSSID(networkCapabilities),
                        networkCapabilities.hasTransport(
                            NetworkCapabilities.TRANSPORT_WIFI,
                        ),
                    )
                }
            }
        }

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _ssid = MutableStateFlow<String?>(null)
    val currentSSID: StateFlow<String?> = _ssid

    init {
        connectivityManager.registerDefaultNetworkCallback(defaultCallback)
        context.registerReceiver(receiver, IntentFilter())
    }

    fun triggerUpdate() {
        connectivityManager.unregisterNetworkCallback(defaultCallback)
        connectivityManager.requestNetwork(networkRequest, defaultCallback)
    }

    private fun getSSID(networkCapabilities: NetworkCapabilities): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            (networkCapabilities.transportInfo as WifiInfo).ssid
        } else {
            wifiManager.connectionInfo.ssid
        }
    }
}