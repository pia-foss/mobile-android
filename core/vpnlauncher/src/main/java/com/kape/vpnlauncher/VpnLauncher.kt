package com.kape.vpnlauncher

import android.content.Context
import android.net.VpnService
import com.kape.connection.ConnectionPrefs
import com.kape.settings.SettingsPrefs
import com.kape.vpnconnect.domain.ConnectionUseCase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

@OptIn(DelicateCoroutinesApi::class)
class VpnLauncher(
    private val context: Context,
    private val connectionPrefs: ConnectionPrefs,
    private val connectionUseCase: ConnectionUseCase,
    private val settingsPrefs: SettingsPrefs,
) : KoinComponent {

    fun launchVpn() {
        val vpnIntent = VpnService.prepare(context)

        if (vpnIntent == null) {
            // vpn permission is provided, initiate a connection
            if (settingsPrefs.isAutomationEnabled() && connectionPrefs.isDisconnectedByUser()) {
                connectionPrefs.disconnectedByUser(false)
                return
            } else {
                connectionPrefs.getSelectedVpnServer()?.let {
                    if (!connectionUseCase.isConnected()) {
                        GlobalScope.launch {
                            connectionUseCase.startConnection(it, false).collect()
                        }
                    }
                }
            }
        } else {
            // TODO: define what happens here
        }
    }

    fun relaunchVpn() = GlobalScope.launch {
        if (isVpnConnected()) {
            connectionUseCase.stopConnection().collect() {
                launchVpn()
            }
        }
    }

    fun stopVpn() = GlobalScope.launch {
        connectionUseCase.stopConnection().collect()
    }

    fun isVpnConnected() = connectionUseCase.isConnected()
}