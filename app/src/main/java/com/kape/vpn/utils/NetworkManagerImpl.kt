package com.kape.vpn.utils

import android.content.Context
import com.kape.contracts.ConnectionStatusProvider
import com.kape.contracts.NetworkManager
import com.kape.data.ConnectionStatus
import com.kape.data.DI
import com.kape.localprefs.prefs.NetworkManagementPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.ui.R
import com.kape.vpnlauncher.VpnLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Singleton([NetworkManager::class])
class NetworkManagerImpl(
    private val context: Context,
    private val networkPrefs: NetworkManagementPrefs,
    private val vpnLauncher: VpnLauncher,
    private val settingsPrefs: SettingsPrefs,
    private val connectionStatusProvider: ConnectionStatusProvider,
    @Named(DI.IO_SCOPE) private val ioScope: CoroutineScope,
) : NetworkManager {
    override fun handleCurrentNetwork(
        ssid: String,
        isWifi: Boolean,
    ) {
        if (settingsPrefs.isAutomationEnabled.value) {
            ioScope.launch {
                networkPrefs.getRuleForNetwork(ssid).first()?.let {
                    applyNetworkRule(it)
                } ?: run {
                    if (isWifi) {
                        networkPrefs
                            .getRuleForNetwork(context.getString(R.string.nmt_open_wifi))
                            .first()
                            ?.let {
                                applyNetworkRule(it)
                            }
                    } else {
                        networkPrefs
                            .getRuleForNetwork(context.getString(R.string.nmt_mobile_data))
                            .first()
                            ?.let {
                                applyNetworkRule(it)
                            }
                    }
                }
            }
        }
    }

    private suspend fun applyNetworkRule(rule: NetworkItem) {
        when (rule.networkBehavior) {
            is NetworkBehavior.AlwaysConnect -> {
                val needsToConnect =
                    listOf(ConnectionStatus.DISCONNECTED, ConnectionStatus.DISCONNECTING).contains(
                        connectionStatusProvider.status.first(),
                    )
                if (needsToConnect) {
                    vpnLauncher.launchVpn()
                }
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