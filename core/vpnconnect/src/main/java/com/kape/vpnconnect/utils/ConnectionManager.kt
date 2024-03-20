package com.kape.vpnconnect.utils

import android.content.Context
import com.kape.shareevents.data.models.KpiConnectionStatus
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConnectionManager(
    private val context: Context,
    private val connectionValues: Map<ConnectionStatus, String>,
    private val submitKpiEventUseCase: SubmitKpiEventUseCase,
) : VPNManagerConnectionListener {
    private val _connectionStatus =
        MutableStateFlow<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus

    private val _serverName = MutableStateFlow("")
    val serverName: StateFlow<String> = _serverName

    private val _serverIso = MutableStateFlow("")
    val serverIso: StateFlow<String> = _serverIso

    private val _connectionStatusTitle =
        MutableStateFlow(context.getString(com.kape.ui.R.string.not_connected))
    val connectionStatusTitle: StateFlow<String> = _connectionStatusTitle

    var isManualConnection: Boolean = false

    fun isConnected(): Boolean = connectionStatus.value == ConnectionStatus.CONNECTED

    fun isConnecting(): Boolean =
        connectionStatus.value == ConnectionStatus.CONNECTING || connectionStatus.value == ConnectionStatus.RECONNECTING

    fun setConnectedServerName(serverName: String, iso: String) {
        _serverName.value = serverName
        _serverIso.value = iso
    }

    override fun handleConnectionStatusChange(status: VPNManagerConnectionStatus) {
        val currentStatus = when (status) {
            VPNManagerConnectionStatus.Disconnecting -> ConnectionStatus.DISCONNECTING
            is VPNManagerConnectionStatus.Disconnected -> ConnectionStatus.DISCONNECTED
            VPNManagerConnectionStatus.Authenticating,
            VPNManagerConnectionStatus.LinkUp,
            VPNManagerConnectionStatus.Configuring,
            VPNManagerConnectionStatus.Connecting,
            -> ConnectionStatus.CONNECTING

            VPNManagerConnectionStatus.Reconnecting -> ConnectionStatus.RECONNECTING
            is VPNManagerConnectionStatus.Connected -> ConnectionStatus.CONNECTED
        }
        _connectionStatus.value = currentStatus
        connectionValues[currentStatus]?.let {
            _connectionStatusTitle.value = String.format(it, serverName.value)
        }
        submitKpiEventUseCase.submitConnectionEvent(
            getKpiConnectionStatus(status),
            isManualConnection,
        )
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
}