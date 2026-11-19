package com.kape.vpnconnect.data

import com.kape.contracts.ConnectionEvents
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.NetworkStatus
import com.kape.contracts.di.Qualifiers
import com.kape.data.ConnectionStatus
import com.kape.data.kpi.KpiConnectionStatus
import com.kape.data.portforwarding.PortForwardingStatus
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.platformsdk.vpn.service.models.KapeVPNConnectionStatus
import com.kape.portforwarding.data.PortForwardingManager
import com.kape.vpnconnect.domain.ClientStateDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Singleton(binds = [ConnectionInfoProvider::class])
class ConnectionInfoProviderImpl(
    private val statusTracker: VpnStatusTracker,
    private val clientState: ClientStateDataSource,
    connectionPrefs: ConnectionPrefs,
    portForwarding: PortForwardingManager,
    networkStatus: NetworkStatus,
    private val events: ConnectionEvents,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) : ConnectionInfoProvider {
    override val status: StateFlow<ConnectionStatus> = statusTracker.status
    override val title: StateFlow<String> = statusTracker.title
    override var name: String = ""
        private set
    override var iso: String = ""
        private set
    override var isManual: Boolean = false
        private set
    override val publicIp: StateFlow<String> = connectionPrefs.clientIp
    override val vpnIp: StateFlow<String> = connectionPrefs.vpnIp
    override val portForwardingStatus: StateFlow<PortForwardingStatus> = portForwarding.portForwardingStatus
    override val port: StateFlow<String> = portForwarding.port

    private var publicIpJob: Job? = null

    init {
        appScope.launch {
            status.collectLatest { status ->
                if (status == ConnectionStatus.DISCONNECTED) refreshPublicIp() else cancelPublicIpRefresh()
                if (status == ConnectionStatus.CONNECTED) clientState.getVpnIp()
            }
        }
        appScope.launch {
            networkStatus.isOnline
                .drop(1)
                .filter { it }
                .collect { if (status.value == ConnectionStatus.DISCONNECTED) refreshPublicIp() }
        }
        appScope.launch {
            statusTracker.sdkStatus.filterNotNull().collectLatest { events.connectionStatusChanged(it.toKpiStatus(), isManual) }
        }
    }

    override fun isConnected(): Boolean = status.value == ConnectionStatus.CONNECTED

    override fun isInConnectState(): Boolean =
        status.value in listOf(ConnectionStatus.CONNECTED, ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING)

    override fun shouldAllowDisconnect(): Boolean =
        status.value != ConnectionStatus.DISCONNECTED && status.value != ConnectionStatus.DISCONNECTING

    override fun requestClientIp() = refreshPublicIp()

    fun updateInfo(
        name: String,
        iso: String,
        isManual: Boolean,
    ) {
        this.name = name
        this.iso = iso
        this.isManual = isManual
    }

    fun resetConnectionInfo() = updateInfo(name = "", iso = "", isManual = false)

    @Synchronized
    private fun refreshPublicIp() {
        if (publicIpJob?.isActive == true) return
        publicIpJob = appScope.launch { clientState.getPublicIp() }
    }

    @Synchronized
    private fun cancelPublicIpRefresh() {
        publicIpJob?.cancel()
        publicIpJob = null
    }

    private fun KapeVPNConnectionStatus.toKpiStatus(): KpiConnectionStatus =
        when (this) {
            KapeVPNConnectionStatus.Connected -> KpiConnectionStatus.Connected
            KapeVPNConnectionStatus.Connecting -> KpiConnectionStatus.Connecting
            KapeVPNConnectionStatus.Reconnecting -> KpiConnectionStatus.Reconnecting
            KapeVPNConnectionStatus.Disconnected, KapeVPNConnectionStatus.Disconnecting -> KpiConnectionStatus.NotConnected
            KapeVPNConnectionStatus.Paused -> KpiConnectionStatus.Paused
        }
}