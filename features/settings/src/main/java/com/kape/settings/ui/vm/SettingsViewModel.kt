package com.kape.settings.ui.vm

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.csi.domain.SendLogUseCase
import com.kape.regions.data.RegionRepository
import com.kape.router.Back
import com.kape.router.EnterFlow
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.settings.SettingsPrefs
import com.kape.settings.data.CustomDns
import com.kape.settings.data.DataEncryption
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import com.kape.settings.utils.PerAppSettingsUtils
import com.kape.shareevents.domain.KpiDataSource
import com.kape.vpnconnect.domain.GetLogsUseCase
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class SettingsViewModel(
    private val prefs: SettingsPrefs,
    private val router: Router,
    private val regionsRepository: RegionRepository,
    val version: String,
    private val kpiDataSource: KpiDataSource,
    private val getDebugLogsUseCase: GetLogsUseCase,
    private val sendLogUseCase: SendLogUseCase,
) : ViewModel(), KoinComponent {

    val launchOnBootEnabled = prefs.isLaunchOnStartupEnabled()
    val connectOnStart = prefs.isConnectOnLaunchEnabled()
    val connectOnUpdate = prefs.isConnectOnAppUpdateEnabled()
    val improvePiaEnabled = mutableStateOf(prefs.isHelpImprovePiaEnabled())
    val vpnExcludedApps = mutableStateOf(prefs.getVpnExcludedApps())
    val appList = mutableStateOf<List<ApplicationInfo>>(emptyList())
    val eventList = mutableStateOf<List<String>>(emptyList())
    val debugLogs = mutableStateOf<List<String>>(emptyList())
    val requestId = mutableStateOf<String?>(null)
    val maceEnabled = mutableStateOf(prefs.isMaceEnabled())
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

    fun exitQuickSettings() {
        router.handleFlow(ExitFlow.QuickSettings)
    }

    fun navigateToConnectionStats() {
        router.handleFlow(EnterFlow.ConnectionStats)
    }

    fun exitConnectionStats() {
        router.handleFlow(ExitFlow.ConnectionStats)
    }

    fun navigateToDebugLogs() {
        router.handleFlow(EnterFlow.DebugLogs)
    }

    fun exitDebugLogs() {
        router.handleFlow(ExitFlow.DebugLogs)
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

    fun toggleMace(enable: Boolean) {
        prefs.setMaceEnabled(enable)
        maceEnabled.value = enable
    }

    fun toggleEnablePortForwarding(enable: Boolean) {
        prefs.setEnablePortForwarding(enable)
    }

    fun toggleAllowLocalNetwork(enable: Boolean) {
        prefs.setAllowLocalTrafficEnabled(enable)
    }

    fun isPortForwardingEnabled() = prefs.isPortForwardingEnabled()

    fun isAllowLocalTrafficEnabled() = prefs.isAllowLocalTrafficEnabled()

    fun getSelectedProtocol(): VpnProtocols = prefs.getSelectedProtocol()

    fun selectProtocol(protocol: VpnProtocols) = prefs.setSelectedProtocol(protocol)

    fun getWireGuardSettings(): WireGuardSettings = prefs.getWireGuardSettings()

    fun getOpenVpnSettings(): OpenVpnSettings = prefs.getOpenVpnSettings()

    fun getCustomDns(): CustomDns = prefs.getCustomDns()

    fun setCustomDns(customDns: CustomDns) = prefs.setCustomDns(customDns = customDns)

    fun setSelectedDnsOption(dnsOptions: DnsOptions) =
        prefs.setSelectedDnsOption(dnsOptions = dnsOptions)

    fun getSelectedDnsOption(): DnsOptions = prefs.getSelectedDnsOption()

    fun toggleQuickSettingKillSwitch(enable: Boolean) = prefs.setQuickSettingKillSwitch(enable)

    fun isQuickSettingKillSwitchEnabled() = prefs.isQuickSettingKillSwitchEnabled()

    fun toggleQuickSettingAutomation(enable: Boolean) = prefs.setQuickSettingAutomation(enable)

    fun isQuickSettingAutomationEnabled() = prefs.isQuickSettingAutomationEnabled()

    fun toggleQuickSettingPrivateBrowser(enable: Boolean) =
        prefs.setQuickSettingPrivateBrowser(enable)

    fun isQuickSettingPrivateBrowserEnabled() = prefs.isQuickSettingPrivateBrowserEnabled()

    fun toggleAutomationEnabled(enable: Boolean) {
        prefs.setAutomationEnabled(enable)
    }

    fun isAutomationEnabled() = prefs.isAutomationEnabled()

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

    fun setPort(port: String) {
        val currentSettings = getOpenVpnSettings()
        currentSettings.port = port
        prefs.setOpenVpnSettings(currentSettings)
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

    fun getRecentEvents() = viewModelScope.launch {
        kpiDataSource.recentEvents().collect {
            eventList.value = it
        }
    }

    fun getDebugLogs() = viewModelScope.launch {
        getDebugLogsUseCase.getDebugLogs().collect {
            debugLogs.value = it
        }
    }

    fun sendLogs() = viewModelScope.launch {
        sendLogUseCase.sendLog().collect {
            requestId.value = it
        }
    }

    fun resetRequestId() {
        requestId.value = null
    }
}



