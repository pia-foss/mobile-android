package com.kape.settings.ui.vm

import androidx.lifecycle.ViewModel
import com.kape.regionselection.data.RegionRepository
import com.kape.router.Back
import com.kape.router.EnterFlow
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.settings.SettingsPrefs
import com.kape.settings.data.DataEncryption
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import org.koin.core.component.KoinComponent

class SettingsViewModel(
    private val prefs: SettingsPrefs,
    private val router: Router,
    private val regionsRepository: RegionRepository,
) : ViewModel(), KoinComponent {

    val launchOnBootEnabled = prefs.isLaunchOnStartupEnabled()
    val connectOnStart = prefs.isConnectOnLaunchEnabled()
    val connectOnUpdate = prefs.isConnectOnAppUpdateEnabled()

    fun navigateUp() {
        router.handleFlow(Back)
    }

    fun navigateToConnection() {
        router.handleFlow(ExitFlow.Settings)
    }

    fun navigateToGeneralSettings() {
        router.handleFlow(EnterFlow.GeneralSettings)
    }

    fun navigateToProtocolSettings() {
        router.handleFlow(EnterFlow.ProtocolSettings)
    }

    fun navigateToNetworkSettings() {
        router.handleFlow(EnterFlow.NetworkSettings)
    }

    fun toggleLaunchOnBoot(enable: Boolean) {
        prefs.setEnableLaunchOnStartup(enable)
    }

    fun toggleConnectOnStart(enable: Boolean) {
        prefs.setEnableConnectOnLaunch(enable)
    }

    fun toggleConnectOnUpdate(enable: Boolean) {
        prefs.setEnableConnectOnAppUpdate(enable)
    }

    fun getSelectedProtocol(): VpnProtocols = prefs.getSelectedProtocol()

    fun selectProtocol(protocol: VpnProtocols) = prefs.setSelectedProtocol(protocol)

    fun getWireGuardSettings(): WireGuardSettings = prefs.getWireGuardSettings()

    fun getOpenVpnSettings(): OpenVpnSettings = prefs.getOpenVpnSettings()

    fun setTransport(transport: Transport) {
        val currentSettings = getOpenVpnSettings()
        currentSettings.transport = transport
        val ports = if (transport == Transport.UDP) {
            regionsRepository.getUdpPorts()
        } else {
            regionsRepository.getTcpPorts()
        }
        currentSettings.port = ports[0].toString()
        prefs.setOpenVpnSettings(currentSettings)
    }

    fun setEncryption(encryption: DataEncryption) {
        val currentSettings = getOpenVpnSettings()
        currentSettings.dataEncryption = encryption
        prefs.setOpenVpnSettings(currentSettings)
    }

    fun setPort(port: String) {
        val currentSettings = getOpenVpnSettings()
        currentSettings.port = port
        prefs.setOpenVpnSettings(currentSettings)
    }

    fun setOpenVpnEnableSmallPackets(enable: Boolean) {
        val currentSettings = getOpenVpnSettings()
        currentSettings.useSmallPackets = enable
        prefs.setOpenVpnSettings(currentSettings)
    }

    fun setWireGuardEnableSmallPackets(enable: Boolean) {
        val currentSettings = getWireGuardSettings()
        currentSettings.useSmallPackets = enable
        prefs.setWireGuardSettings(currentSettings)
    }

    fun getPorts(): List<String> {
        val listOfPorts = mutableListOf<String>()
        when (getOpenVpnSettings().transport) {
            Transport.UDP -> {
                regionsRepository.getUdpPorts().distinct().forEach {
                    listOfPorts.add(it.toString())
                }
            }

            Transport.TCP -> {
                regionsRepository.getTcpPorts().distinct().forEach {
                    listOfPorts.add(it.toString())
                }
            }
        }
        return listOfPorts
    }
}