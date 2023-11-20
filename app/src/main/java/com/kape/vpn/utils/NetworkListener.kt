package com.kape.vpn.utils

import android.content.Context
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.net.wifi.WifiInfo
import android.os.Build
import com.kape.networkmanagement.NetworkManagementPrefs
import com.kape.networkmanagement.R
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.domain.NetworkInfoSource
import com.kape.vpn.receiver.OnRulesChangedReceiver

class NetworkListener(
    private val context: Context,
    private val networkPrefs: NetworkManagementPrefs,
    private val vpnLauncher: VpnLauncher,
    private val networkInfoSource: NetworkInfoSource,
) {
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
                            networkInfoSource.getSSID(networkCapabilities),
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
                        networkInfoSource.getSSID(networkCapabilities),
                        networkCapabilities.hasTransport(
                            TRANSPORT_WIFI,
                        ),
                    )
                }
            }
        }

    fun register() {
        networkInfoSource.registerDefaultCallback(callback, context, OnRulesChangedReceiver())
    }

    fun triggerUpdate() {
        networkInfoSource.triggerUpdate(callback)
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