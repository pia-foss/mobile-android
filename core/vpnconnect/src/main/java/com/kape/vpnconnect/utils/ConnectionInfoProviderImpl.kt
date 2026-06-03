package com.kape.vpnconnect.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionStatusProvider
import com.kape.data.ConnectionStatus
import com.kape.data.DI
import com.kape.data.kpi.KpiConnectionStatus
import com.kape.data.portforwarding.PortForwardingStatus
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.portforwarding.domain.PortForwardingUseCase
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

class ConnectionInfoProviderImpl(
    private val connectionStatusProvider: ConnectionStatusProvider,
    private val clientStateDataSource: ClientStateDataSource,
    private val connectionPrefs: ConnectionPrefs,
    private val submitKpiEventUseCase: SubmitKpiEventUseCase,
    private val portForwardingUseCase: PortForwardingUseCase,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
    @Named(DI.MAIN_DISPATCHER) private val mainDispatcher: CoroutineDispatcher,
) : ConnectionInfoProvider {
    private val ioScope = CoroutineScope(ioDispatcher)
    private val currentConnectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    override val connectionInfoState = connectionStatusProvider.state
    override var name: String = ""
    override var iso: String = ""
    override var isManual: Boolean = false
    override val publicIp: StateFlow<String> = connectionPrefs.clientIp
    override val vpnIp: StateFlow<String> = connectionPrefs.vpnIp
    override val portForwardingStatus: StateFlow<PortForwardingStatus> =
        portForwardingUseCase.portForwardingStatus.asStateFlow()
    override val port: StateFlow<String> = portForwardingUseCase.port

    init {
        ioScope.launch {
            connectionStatusProvider.state
                .distinctUntilChangedBy { it.status == ConnectionStatus.CONNECTED || it.status == ConnectionStatus.DISCONNECTED }
                .collectLatest { (status, title, vpnManagerConnectionStatus) ->
                    currentConnectionStatus.update { status }
                    vpnManagerConnectionStatus?.let {
                        submitKpiEventUseCase.submitConnectionEvent(
                            getKpiConnectionStatus(it),
                            isManual,
                        )
                    }
                    if (status == ConnectionStatus.DISCONNECTED) {
                        clientStateDataSource.getPublicIp()
                    }
                    if (status == ConnectionStatus.CONNECTED) {
                        clientStateDataSource.getVpnIp()
                    }
                }
        }
    }

    override fun isConnected(): Boolean = currentConnectionStatus.value == ConnectionStatus.CONNECTED

    override fun isInConnectState(): Boolean =
        listOf(
            ConnectionStatus.CONNECTED,
            ConnectionStatus.CONNECTING,
            ConnectionStatus.RECONNECTING,
        ).contains(currentConnectionStatus.value)

    override fun isNotDisconnected(): Boolean = currentConnectionStatus.value != ConnectionStatus.DISCONNECTED

    override fun updateInfo(
        name: String,
        iso: String,
        isManual: Boolean,
    ) {
        this.name = name
        this.iso = iso
        this.isManual = isManual
    }

    override fun resetConnectionInfo() {
        this.name = ""
        this.iso = ""
        this.isManual = false
    }

    override fun getTopBarConnectionColor(scheme: ColorScheme): Color =
        when (connectionInfoState.value.status) {
            ConnectionStatus.ERROR -> scheme.statusBarError()
            ConnectionStatus.CONNECTED -> scheme.statusBarConnected()
            ConnectionStatus.DISCONNECTED, ConnectionStatus.DISCONNECTING ->
                scheme.statusBarDefault(
                    scheme,
                )

            ConnectionStatus.RECONNECTING, ConnectionStatus.CONNECTING -> scheme.statusBarConnecting()
        }

    override fun requestClientIp() {
        ioScope.launch {
            clientStateDataSource.getPublicIp()
        }
    }

    private fun getKpiConnectionStatus(status: VPNManagerConnectionStatus): KpiConnectionStatus =
        when (status) {
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