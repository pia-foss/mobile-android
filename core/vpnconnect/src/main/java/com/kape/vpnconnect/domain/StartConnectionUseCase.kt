package com.kape.vpnconnect.domain

import com.kape.contracts.ConnectionConfigurationUseCase
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionStatusProvider
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal class StartConnectionUseCase(
    private val connectionSource: ConnectionDataSource,
    private val connectionInfoProvider: ConnectionInfoProvider,
    private val connectionPrefs: ConnectionPrefs,
    private val startShadowsocksUseCase: StartShadowsocksUseCase,
    private val stopShadowsocksUseCase: StopShadowsocksUseCase,
    private val connectionConfigurationUseCase: ConnectionConfigurationUseCase,
    private val startPortForwardingUseCase: StartPortForwardingUseCase,
) {
    val state = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    private var currentJob: Job? = null

    fun start(server: VpnServer, isManualConnection: Boolean, scope: CoroutineScope): Job {
        print("--- startConnection?")
        currentJob?.cancel() // cancel previous if any
        val job = scope.launch {
            try {
                connectionInfoProvider.updateInfo(server.name, server.iso, isManualConnection)
                connectionPrefs.setSelectedVpnServer(server)
                connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp)

                // --- Step 1: Start Shadowsocks ---
                state.value = ConnectionState.ConnectingShadowsocks
                val shadowsocksSuccess = startShadowsocksUseCase()
                ensureActive() // cancellation checkpoint

                if (!shadowsocksSuccess) {
                    state.value = ConnectionState.Error("Shadowsocks failed")
                    return@launch
                }
                state.value = ConnectionState.ShadowsocksConnected

                // --- Step 2: Start VPN ---
                state.value = ConnectionState.ConnectingVPN
                val vpnConnected = startVpn(server)
                ensureActive()
                if (!vpnConnected) {
                    stopShadowsocksUseCase()
                    state.value = ConnectionState.Error("VPN connection failed")
                    return@launch
                }
                state.value = ConnectionState.VPNConnected

                // --- Step 3: Start Port Forwarding ---
                state.value = ConnectionState.BindingPort
                val portSuccess = startPortForwardingUseCase()
                ensureActive()
                if (!portSuccess) {
                    state.value = ConnectionState.Error("Port forwarding failed")
                    return@launch
                }

                state.value = ConnectionState.Connected
            } catch (e: CancellationException) {
                // --- Rollback on cancellation ---
                stopShadowsocksUseCase()
                connectionSource.stopConnection()
                state.value = ConnectionState.Cancelled
                throw e
            } catch (e: Exception) {
                stopShadowsocksUseCase()
                connectionSource.stopConnection()
                state.value = ConnectionState.Error(e.message ?: "Unknown error")
            }
        }

        currentJob = job
        return job
    }

    fun cancel() {
        currentJob?.cancel()
    }

    private suspend fun startVpn(server: VpnServer): Boolean {
        val connected = connectionSource.startConnection(
            connectionConfigurationUseCase.generateConnectionConfiguration(server),
        )
        currentCoroutineContext().ensureActive()
        if (connected) {
            startPortForwardingUseCase()
        }
        return connected
    }

    sealed class ConnectionState {
        object Idle : ConnectionState()
        object ConnectingShadowsocks : ConnectionState()
        object ShadowsocksConnected : ConnectionState()
        object ConnectingVPN : ConnectionState()
        object VPNConnected : ConnectionState()
        object BindingPort : ConnectionState()
        object Connected : ConnectionState()
        data class Error(val reason: String) : ConnectionState()
        object Cancelled : ConnectionState()
    }
}