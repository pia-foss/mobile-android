package com.kape.settings.ui.vm

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
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
import com.kape.settings.utils.PerAppSettingsUtils
import org.koin.core.component.KoinComponent

class SettingsViewModel(
    private val prefs: SettingsPrefs,
    private val router: Router,
    private val regionsRepository: RegionRepository,
    val version: String,
) : ViewModel(), KoinComponent {

    val launchOnBootEnabled = prefs.isLaunchOnStartupEnabled()
    val connectOnStart = prefs.isConnectOnLaunchEnabled()
    val connectOnUpdate = prefs.isConnectOnAppUpdateEnabled()
    val improvePiaEnabled = mutableStateOf(prefs.isHelpImprovePiaEnabled())
    val vpnExcludedApps = mutableStateOf(prefs.getVpnExcludedApps())
    val appList = mutableStateOf<List<ApplicationInfo>>(emptyList())
    private var installedApps = listOf<ApplicationInfo>()

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

    fun navigateToPrivacySettings() {
        router.handleFlow(EnterFlow.PrivacySettings)
    }

    fun navigateToHelpSettings() {
        router.handleFlow(EnterFlow.HelpSettings)
    }

    fun navigateToAutomation() {
        router.handleFlow(EnterFlow.AutomationSettings)
    }

    fun navigateToKillSwitch() {
        router.handleFlow(EnterFlow.KillSwitchSettings)
    }

    fun exitPrivacySettings() {
        router.handleFlow(ExitFlow.PrivacySettings)
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

    fun toggleImprovePia(enable: Boolean) {
        prefs.setHelpImprovePiaEnabled(enable)
        improvePiaEnabled.value = enable
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

    fun addToVpnExcludedApps(app: String) {
        val newList = mutableListOf<String>()
        newList.addAll(prefs.getVpnExcludedApps())
        newList.add(app)
        prefs.setVpnExcludedApps(newList)
        vpnExcludedApps.value = prefs.getVpnExcludedApps()
    }

    fun removeFromVpnExcludedApps(app: String) {
        val newList = mutableListOf<String>()
        newList.addAll(prefs.getVpnExcludedApps())
        newList.remove(app)
        prefs.setVpnExcludedApps(newList)
        vpnExcludedApps.value = prefs.getVpnExcludedApps()
    }

    fun getInstalledApplications(packageManager: PackageManager) {
        installedApps = PerAppSettingsUtils.getInstalledApps(packageManager = packageManager)
        appList.value = installedApps
    }

    fun filterAppsByName(value: String, packageManager: PackageManager) {
        if (value.isEmpty()) {
            appList.value = installedApps
        } else {
            appList.value = installedApps.filter {
                it.loadLabel(packageManager).toString().lowercase().contains(value.lowercase())
            }
        }
    }
}



