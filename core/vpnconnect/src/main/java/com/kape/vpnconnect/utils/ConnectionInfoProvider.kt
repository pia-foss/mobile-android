package com.kape.vpnconnect.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.kape.data.ConnectionStatus
import com.kape.data.DI
import com.kape.data.kpi.KpiConnectionStatus
import com.kape.data.portforwarding.PortForwardingStatus
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.NO_IP
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.ui.theme.statusBarConnected
import com.kape.ui.theme.statusBarConnecting
import com.kape.ui.theme.statusBarDefault
import com.kape.ui.theme.statusBarError
import com.kape.vpnconnect.domain.ClientStateDataSource
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named

class ConnectionInfoProvider(
    private val connectionStatusProvider: ConnectionStatusProvider,
    private val clientStateDataSource: ClientStateDataSource,
    private val connectionPrefs: ConnectionPrefs,
    private val submitKpiEventUseCase: SubmitKpiEventUseCase,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
    @Named(DI.MAIN_DISPATCHER) private val mainDispatcher: CoroutineDispatcher,
) {
    val connectionState = connectionStatusProvider.state
    private val _connectionInfoState: MutableStateFlow<VpnConnectionInfo> = MutableStateFlow(
        VpnConnectionInfo(),
    )
    val state: StateFlow<VpnConnectionInfo> = _connectionInfoState.asStateFlow()

    init {
        CoroutineScope(ioDispatcher).launch {
            connectionStatusProvider.state
                .distinctUntilChangedBy { it.status == ConnectionStatus.CONNECTED || it.status == ConnectionStatus.DISCONNECTED }
                .collectLatest {
                    it.vpnManagerConnectionStatus?.let {
                        submitKpiEventUseCase.submitConnectionEvent(
                            getKpiConnectionStatus(it),
                            _connectionInfoState.value.isManual,
                        )
                    }
                    if (it.status == ConnectionStatus.DISCONNECTED) {
                        val ip = clientStateDataSource.getPublicIp()
                        _connectionInfoState.update { it.copy(publicIp = ip, vpnIp = NO_IP) }
                    }
                    if (it.status == ConnectionStatus.CONNECTED) {
                        val vpnIp = clientStateDataSource.getVpnIp()
                        _connectionInfoState.update { it.copy(vpnIp = vpnIp) }
                    }
                }
        }
    }

    fun isConnected(): Boolean = connectionState.value == ConnectionStatus.CONNECTED

    fun isInConnectState(): Boolean = listOf(
        ConnectionStatus.CONNECTED, ConnectionStatus.CONNECTING,
        ConnectionStatus.RECONNECTING,
    ).contains(connectionState.value.status)

    fun isNotDisconnected() = connectionState.value.status != ConnectionStatus.DISCONNECTED

    fun updateInfo(name: String, iso: String, isManual: Boolean) {
        _connectionInfoState.update { it.copy(name = name, iso = iso, isManual = isManual) }
    }

    fun resetConnectionInfo() {
        _connectionInfoState.update { VpnConnectionInfo() }
    }

    fun getTopBarConnectionColor(scheme: ColorScheme): Color {
        return when (connectionState.value.status) {
            ConnectionStatus.ERROR -> scheme.statusBarError()
            ConnectionStatus.CONNECTED -> scheme.statusBarConnected()
            ConnectionStatus.DISCONNECTED, ConnectionStatus.DISCONNECTING -> scheme.statusBarDefault(
                scheme,
            )

            ConnectionStatus.RECONNECTING, ConnectionStatus.CONNECTING -> scheme.statusBarConnecting()
        }
    }

    private fun getKpiConnectionStatus(status: VPNManagerConnectionStatus): KpiConnectionStatus {
        return when (status) {
            VPNManagerConnectionStatus.Disconnecting,
            is VPNManagerConnectionStatus.Disconnected,
                -> KpiConnectionStatus.NotConnected

            VPNManagerConnectionStatus.Authenticating,
            VPNManagerConnectionStatus.LinkUp,
            VPNManagerConnectionStatus.Configuring,
            VPNManagerConnectionStatus.Connecting,
                -> KpiConnectionStatus.Connecting

            VPNManagerConnectionStatus.Reconnecting -> KpiConnectionStatus.Reconnecting
            is VPNManagerConnectionStatus.Connected -> KpiConnectionStatus.Connected
        }
    }

    data class VpnConnectionInfo(
        val name: String = "",
        val iso: String = "",
        val isManual: Boolean = false,
        val publicIp: String = NO_IP,
        val vpnIp: String = NO_IP,
        val portforwardingStatus: PortForwardingStatus = PortForwardingStatus.NoPortForwarding,
        val port: String = "",
    )
}