package com.kape.vpnlauncher

import android.content.Context
import android.net.VpnService
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpnconnect.domain.StartConnectionUseCase
import com.kape.vpnconnect.domain.StopConnectionUseCase
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext

class VpnLauncher(
    private val context: Context,
    private val connectionPrefs: ConnectionPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val regionListProvider: RegionListProvider,
    private val startConnectionUseCase: StartConnectionUseCase,
    private val stopConnectionUseCase: StopConnectionUseCase,
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
                launch {
                    server?.let {
                        initiateConnection(it)
                    } ?: run {
                        if (regionListProvider.isDefaultList.first().not()) {
                            initiateConnection(regionListProvider.getOptimalServer())
                        } else {
                            regionListProvider.updateServerLatencies(
                                isConnected = false,
                                isUserInitiated = false,
                            )
                            initiateConnection(regionListProvider.getOptimalServer())
                        }
                    }
                }
            }
        } else {
            // TODO: define what happens here
        }
    }

    private suspend fun initiateConnection(server: VpnServer) =
        startConnectionUseCase(server, false)

    fun stopVpn() =
        launch {
            stopConnectionUseCase()
        }
}