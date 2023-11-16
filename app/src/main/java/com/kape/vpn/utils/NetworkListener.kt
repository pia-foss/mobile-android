package com.kape.vpn.utils

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.kape.networkmanagement.NetworkManagementPrefs
import com.kape.networkmanagement.R
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.vpn.receiver.OnRulesChangedReceiver

class NetworkListener(
    private val context: Context,
    private val networkPrefs: NetworkManagementPrefs,
    private val vpnLauncher: VpnLauncher,
) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val callback: NetworkCallback =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            object : NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    if (networkCapabilities.transportInfo is WifiInfo) {
                        handleCurrentNetwork(
                            (networkCapabilities.transportInfo as WifiInfo).ssid,
                            networkCapabilities.hasTransport(
                                TRANSPORT_WIFI,
                            ),
                        )
                    }
                }
            }
        } else {
            object : NetworkCallback() {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    handleCurrentNetwork(
                        wifiManager.connectionInfo.ssid,
                        networkCapabilities.hasTransport(
                            TRANSPORT_WIFI,
                        ),
                    )
                }
            }
        }

    fun register() {
        connectivityManager.registerDefaultNetworkCallback(callback)
        context.registerReceiver(OnRulesChangedReceiver(), IntentFilter())
    }

    fun triggerUpdate() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .addTransportType(TRANSPORT_WIFI)
            .addTransportType(TRANSPORT_CELLULAR)
            .build()
        connectivityManager.requestNetwork(networkRequest, callback)
    }

    private fun handleCurrentNetwork(ssid: String, isWifi: Boolean) {
        networkPrefs.getRuleForNetwork(ssid)?.let {
            applyNetworkRule(it)
        } ?: run {
            if (isWifi) {
                networkPrefs.getRuleForNetwork(context.getString(R.string.nmt_open_wifi))?.let {
                    applyNetworkRule(it)
                }
            } else {
                networkPrefs.getRuleForNetwork(context.getString(R.string.nmt_mobile_data))?.let {
                    applyNetworkRule(it)
                }
            }
        }
    }

    private fun applyNetworkRule(rule: NetworkItem) {
        when (rule.networkBehavior) {
            is NetworkBehavior.AlwaysConnect -> {
                vpnLauncher.launchVpn()
            }

            is NetworkBehavior.AlwaysDisconnect -> {
                vpnLauncher.stopVpn()
            }

            is NetworkBehavior.RetainState -> {
                // do nothing - retaining state
            }
        }
    }
}