package com.kape.vpn.utils

import android.content.Context
import com.kape.networkmanagement.NetworkManagementPrefs
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.settings.SettingsPrefs
import com.kape.ui.R
import com.kape.vpnlauncher.VpnLauncher

class NetworkManager(
    private val context: Context,
    private val networkPrefs: NetworkManagementPrefs,
    private val vpnLauncher: VpnLauncher,
    private val settingsPrefs: SettingsPrefs,
) {

    fun handleCurrentNetwork(ssid: String, isWifi: Boolean) {
        if (settingsPrefs.isAutomationEnabled()) {
            networkPrefs.getRuleForNetwork(ssid)?.let {
                applyNetworkRule(it)
            } ?: run {
                if (isWifi) {
                    networkPrefs.getRuleForNetwork(context.getString(R.string.nmt_open_wifi))?.let {
                        applyNetworkRule(it)
                    }
                } else {
                    networkPrefs.getRuleForNetwork(context.getString(R.string.nmt_mobile_data))
                        ?.let {
                            applyNetworkRule(it)
                        }
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