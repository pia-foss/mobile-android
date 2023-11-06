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
import com.kape.settings.data.WidgetSettings
import com.kape.settings.data.WireGuardSettings
import com.kape.settings.domain.IsNumericIpAddressUseCase
import com.kape.settings.utils.PerAppSettingsUtils
import com.kape.settings.utils.SettingsStep
import com.kape.shareevents.domain.KpiDataSource
import com.kape.ui.utils.defaultWidgetBackgroundColor
import com.kape.ui.utils.defaultWidgetDownloadColor
import com.kape.ui.utils.defaultWidgetTextColor
import com.kape.ui.utils.defaultWidgetUploadColor
import com.kape.ui.utils.toColorString
import com.kape.vpnconnect.domain.GetLogsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val isNumericIpAddressUseCase: IsNumericIpAddressUseCase,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow<SettingsStep>(SettingsStep.Main)
    val state: StateFlow<SettingsStep> = _state

    val launchOnBootEnabled = prefs.isLaunchOnStartupEnabled()
    val connectOnStart = prefs.isConnectOnLaunchEnabled()
    val connectOnUpdate = prefs.isConnectOnAppUpdateEnabled()
    val improvePiaEnabled = mutableStateOf(prefs.isHelpImprovePiaEnabled())
    val vpnExcludedApps = mutableStateOf(prefs.getVpnExcludedApps())
    val isAllowLocalTrafficEnabled = mutableStateOf(prefs.isAllowLocalTrafficEnabled())
    val appList = mutableStateOf<List<ApplicationInfo>>(emptyList())
    val eventList = mutableStateOf<List<String>>(emptyList())
    val debugLogs = mutableStateOf<List<String>>(emptyList())
    val requestId = mutableStateOf<String?>(null)
    val maceEnabled = mutableStateOf(prefs.isMaceEnabled())
    private val isDnsNumeric = mutableStateOf(true)
    private var installedApps = listOf<ApplicationInfo>()

    fun navigateUp() {
        router.handleFlow(Back)
    }

    fun navigateToConnection() {
        router.handleFlow(ExitFlow.Settings)
    }

    fun navigateToGeneralSettings() = viewModelScope.launch {
        _state.emit(SettingsStep.General)
    }

    fun navigateToProtocolSettings() = viewModelScope.launch {
        _state.emit(SettingsStep.Protocol)
    }

    fun navigateToNetworkSettings() = viewModelScope.launch {
        _state.emit(SettingsStep.Network)
    }

    fun navigateToPrivacySettings() = viewModelScope.launch {
        _state.emit(SettingsStep.Privacy)
    }

    fun navigateToHelpSettings() = viewModelScope.launch {
        _state.emit(SettingsStep.Help)
    }

    fun navigateToAutomationSettings() = viewModelScope.launch {
        _state.emit(SettingsStep.Automation)
    }

    fun navigateToKillSwitch() = viewModelScope.launch {
        _state.emit(SettingsStep.KillSwitch)
    }

    fun exitPrivacySettings() = viewModelScope.launch {
        _state.emit(SettingsStep.Privacy)
    }

    fun exitQuickSettings() {
        router.handleFlow(ExitFlow.QuickSettings)
    }

    fun navigateToConnectionStats() = viewModelScope.launch {
        _state.emit(SettingsStep.ConnectionStats)
    }

    fun exitConnectionStats() = viewModelScope.launch {
        _state.emit(SettingsStep.Help)
    }

    fun navigateToDebugLogs() = viewModelScope.launch {
        _state.emit(SettingsStep.DebugLogs)
    }

    fun exitDebugLogs() = viewModelScope.launch {
        _state.emit(SettingsStep.Help)
    }

    fun navigateToWidgetSettings() = viewModelScope.launch {
        _state.emit(SettingsStep.Widget)
    }

    fun exitWidgetSettings() = viewModelScope.launch {
        _state.emit(SettingsStep.General)
    }

    private fun navigateToAutomation() = router.handleFlow(EnterFlow.Automation)

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
        isAllowLocalTrafficEnabled.value = enable
    }

    fun isPortForwardingEnabled() = prefs.isPortForwardingEnabled()

    fun getSelectedProtocol(): VpnProtocols = prefs.getSelectedProtocol()

    fun selectProtocol(protocol: VpnProtocols) = prefs.setSelectedProtocol(protocol)

    fun getWireGuardSettings(): WireGuardSettings = prefs.getWireGuardSettings()

    fun getOpenVpnSettings(): OpenVpnSettings = prefs.getOpenVpnSettings()

    fun getCustomDns(): CustomDns = prefs.getCustomDns()

    fun setCustomDns(customDns: CustomDns) = prefs.setCustomDns(customDns = customDns)

    fun isDnsNumeric(ipAddress: String): Boolean {
        isDnsNumeric.value = isNumericIpAddressUseCase.invoke(ipAddress = ipAddress)
        return isDnsNumeric.value
    }

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
        if (enable) {
            navigateToAutomation()
        }
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

    fun getWidgetSettings(): WidgetSettings {
        return prefs.getWidgetSettings() ?: run {
            WidgetSettings(
                defaultWidgetBackgroundColor().toColorString(),
                defaultWidgetTextColor().toColorString(),
                defaultWidgetUploadColor().toColorString(),
                defaultWidgetDownloadColor().toColorString(),
                8,
            )
        }
    }

    fun updateWidgetSettings(settings: WidgetSettings) {
        prefs.setWidgetSettings(settings)
    }
}



