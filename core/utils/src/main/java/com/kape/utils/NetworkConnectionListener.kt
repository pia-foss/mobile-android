package com.kape.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.kape.contracts.NetworkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkConnectionListener(
    context: Context,
    private val networkManager: NetworkManager,
    private val sdkInt: Int = Build.VERSION.SDK_INT,
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val networkCallback =
        if (sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onAvailable(network: Network) {
                    _isConnected.value = true
                }

                override fun onLost(network: Network) = onNetworkLost()

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    val isWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    if (networkCapabilities.transportInfo is WifiInfo) {
                        val ssid = getSSID(networkCapabilities)
                        _ssid.value = ssid
                        handleNetwork(network, ssid, isWifi)
                    } else {
                        handleNetwork(network, "", isWifi)
                    }
                }
            }
        } else {
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isConnected.value = true
                }

                override fun onLost(network: Network) = onNetworkLost()

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    val ssid = getSSID(networkCapabilities)
                    _ssid.value = ssid
                    handleNetwork(
                        network,
                        ssid,
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
                    )
                }
            }
        }

    // Tracks the underlying Wi-Fi/cellular network rather than the default network, which becomes
    // our own VPN once it is up and would hide the SSID and network switches from the rules.
    private val networkRequest =
        NetworkRequest
            .Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

    private val _isConnected = MutableStateFlow(true)

    val isConnected = _isConnected.asStateFlow()

    private val _ssid = MutableStateFlow<String?>(null)
    val ssid = _ssid

    // onCapabilitiesChanged fires for things like signal strength updates, so rules are only applied
    // when the network itself changes.
    @Volatile
    private var lastHandledNetwork: HandledNetwork? = null

    init {
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    /**
     * Re-registers the callback so the current network is delivered again and the rules are
     * re-applied even if the network did not change (e.g. after the rules or the automation toggle changed).
     */
    fun triggerUpdate() {
        lastHandledNetwork = null
        connectivityManager.unregisterNetworkCallback(networkCallback)
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    private fun handleNetwork(
        network: Network,
        ssid: String,
        isWifi: Boolean,
    ) {
        val previous = lastHandledNetwork
        val current = HandledNetwork(network, ssid, isWifi)
        if (current == previous) return
        lastHandledNetwork = current
        networkManager.handleCurrentNetwork(
            ssid = ssid,
            isWifi = isWifi,
            isNetworkChange = previous != null && previous.network != network,
        )
    }

    private fun onNetworkLost() {
        _isConnected.value =
            connectivityManager
                .getNetworkCapabilities(connectivityManager.activeNetwork)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    private fun getSSID(networkCapabilities: NetworkCapabilities): String =
        if (sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            (networkCapabilities.transportInfo as WifiInfo).ssid
        } else {
            wifiManager.connectionInfo.ssid
        }

    private data class HandledNetwork(
        val network: Network,
        val ssid: String,
        val isWifi: Boolean,
    )
}