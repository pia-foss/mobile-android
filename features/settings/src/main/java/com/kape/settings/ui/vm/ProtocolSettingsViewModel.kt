package com.kape.settings.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.localprefs.api.DipStore
import com.kape.localprefs.api.SettingsStore
import com.kape.localprefs.data.settings.AutomaticSettings
import com.kape.regions.vpn.domain.usecase.GetVpnServerInfoUseCase
import com.kape.settings.data.DataEncryption
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import com.kape.settings.domain.usecase.ReconnectPrompt
import com.kape.settings.domain.usecase.SelectProtocolUseCase
import com.kape.settings.domain.usecase.SetTransportUseCase
import com.kape.settings.domain.usecase.dedicatedIpOpenVpnPorts
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ProtocolSettingsViewModel(
    private val prefs: SettingsStore,
    private val selectProtocolUseCase: SelectProtocolUseCase,
    private val setTransportUseCase: SetTransportUseCase,
    val reconnectPrompt: ReconnectPrompt,
    getServerInfo: GetVpnServerInfoUseCase,
    dipPrefs: DipStore,
) : ViewModel() {
    val selectedProtocol: StateFlow<VpnProtocols> = prefs.selectedProtocol
    val wireGuardSettings: StateFlow<WireGuardSettings> = prefs.wireGuardSettings
    val openVpnSettings: StateFlow<OpenVpnSettings> = prefs.openVpnSettings
    val autoSettings: StateFlow<AutomaticSettings> = prefs.autoSettings

    val ports: StateFlow<Map<Int, String>> =
        combine(openVpnSettings, flow { emit(getServerInfo()) }) { settings, info ->
            when (settings.transport) {
                Transport.UDP -> info.udpPorts.orEmpty()
                Transport.TCP -> info.tcpPorts.orEmpty()
                Transport.AUTO -> emptyList()
            }.distinct().associateWith { it.toString() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyMap())

    val dipPorts: StateFlow<List<Int>> =
        combine(
            dipPrefs.dedicatedIpPorts,
            openVpnSettings,
        ) { ports, settings -> dedicatedIpOpenVpnPorts(ports, settings.transport) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    fun selectProtocol(protocol: VpnProtocols) {
        viewModelScope.launch { selectProtocolUseCase(protocol) }
    }

    fun setTransport(transport: Transport) =
        change {
            setTransportUseCase(transport)
        }

    fun setEncryption(encryption: DataEncryption) = changeOpenVpn { it.copy(dataEncryption = encryption) }

    fun setPort(port: String) = changeOpenVpn { it.copy(port = port) }

    fun setOpenVpnEnableSmallPackets(enabled: Boolean) = changeOpenVpn { it.copy(useSmallPackets = enabled) }

    fun setWireGuardEnableSmallPackets(enabled: Boolean) = changeWireGuard { it.copy(useSmallPackets = enabled) }

    fun setWireGuardEnableAmnezia(enabled: Boolean) = changeWireGuard { it.copy(useAmnezia = enabled) }

    fun setAutoEnableSmallPackets(enabled: Boolean) =
        change {
            val current = prefs.getAutoSettingsNow()
            prefs.setAutoSettings(current.copy(useSmallPackets = enabled))
            current.useSmallPackets != enabled
        }

    private fun changeOpenVpn(update: (OpenVpnSettings) -> OpenVpnSettings) =
        change {
            val current = prefs.getOpenVpnSettingsNow()
            val updated = update(current)
            prefs.setOpenVpnSettings(updated)
            updated != current
        }

    private fun changeWireGuard(update: (WireGuardSettings) -> WireGuardSettings) =
        change {
            val current = prefs.getWireGuardSettingsNow()
            val updated = update(current)
            prefs.setWireGuardSettings(updated)
            updated != current
        }

    private fun change(apply: suspend () -> Boolean) {
        viewModelScope.launch {
            if (apply()) reconnectPrompt.onSettingChanged()
        }
    }
}

private const val STOP_TIMEOUT_MS = 5_000L