package com.kape.vpnlauncher

import android.content.Context
import android.net.VpnService
import com.kape.connection.ConnectionPrefs
import com.kape.settings.SettingsPrefs
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext

class VpnLauncher(
    private val context: Context,
    private val connectionPrefs: ConnectionPrefs,
    private val connectionUseCase: ConnectionUseCase,
    private val settingsPrefs: SettingsPrefs,
    private val regionListProvider: RegionListProvider,
) : CoroutineScope,
    KoinComponent {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    fun launchVpn() {
        val vpnIntent = VpnService.prepare(context)

        if (vpnIntent == null) {
            // vpn permission is provided, initiate a connection
            if (settingsPrefs.isAutomationEnabled() && connectionPrefs.isDisconnectedByUser()) {
                connectionPrefs.disconnectedByUser(false)
                return
            } else {
                val server: VpnServer? = connectionPrefs.getSelectedVpnServer()
                if (!connectionUseCase.isConnected()) {
                    launch {
                        server?.let {
                            initiateConnection(it)
                        } ?: run {
                            if (regionListProvider.isDefaultList().not()) {
                                initiateConnection(regionListProvider.getOptimalServer())
                            } else {
                                regionListProvider
                                    .updateServerLatencies(
                                        isConnected = false,
                                        isUserInitiated = false,
                                    ).collect {
                                        initiateConnection(regionListProvider.getOptimalServer())
                                    }
                            }
                        }
                    }
                }
            }
        } else {
            // TODO: define what happens here
        }
    }

    private suspend fun initiateConnection(server: VpnServer) = connectionUseCase
        .startConnection(
            server,
            false,
        ).collect()

    fun stopVpn() =
        launch {
            connectionUseCase.stopConnection().collect()
        }

    fun isVpnConnected() = connectionUseCase.isConnected()
}