package com.kape.vpn.utils

import android.content.Context
import android.net.VpnService
import com.kape.connection.ConnectionPrefs
import com.kape.vpnconnect.domain.ConnectionUseCase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class VpnLauncher(
    private val context: Context,
    private val connectionPrefs: ConnectionPrefs,
    private val connectionUseCase: ConnectionUseCase,
) : KoinComponent {

    @OptIn(DelicateCoroutinesApi::class)
    fun launchVpn() {
        val vpnIntent = VpnService.prepare(context)

        if (vpnIntent == null) {
            // vpn permission is provided, initiate a connection
            connectionPrefs.getSelectedServer()?.let {
                if (!connectionUseCase.isConnected()) {
                    GlobalScope.launch {
                        connectionUseCase.startConnection(it, false).collect()
                    }
                }
            }
        } else {
            // TODO: define what happens here
        }
    }
}