package com.kape.settings.ui.vm

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.connection.ConnectionPrefs
import com.kape.csi.domain.SendLogUseCase
import com.kape.location.data.LocationPermissionManager
import com.kape.router.Back
import com.kape.router.EnterFlow
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.settings.SettingsPrefs
import com.kape.settings.data.CustomDns
import com.kape.settings.data.CustomObfuscation
import com.kape.settings.data.DataEncryption
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import com.kape.settings.domain.IsNumericIpAddressUseCase
import com.kape.settings.utils.PerAppSettingsUtils
import com.kape.settings.utils.SettingsStep
import com.kape.shareevents.domain.KpiDataSource
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnconnect.domain.GetLogsUseCase
import com.kape.vpnregions.data.VpnRegionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class SettingsViewModel(
    private val prefs: SettingsPrefs,
    private val connectionPrefs: ConnectionPrefs,
    private val router: Router,
    private val regionsRepository: VpnRegionRepository,
    val version: String,
    private val kpiDataSource: KpiDataSource,
    private val getDebugLogsUseCase: GetLogsUseCase,
    private val sendLogUseCase: SendLogUseCase,
    private val isNumericIpAddressUseCase: IsNumericIpAddressUseCase,
    private val locationPermissionManager: LocationPermissionManager,
    private val connectionUseCase: ConnectionUseCase,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow<SettingsStep>(SettingsStep.Main)
    val state: StateFlow<SettingsStep> = _state

    val launchOnBootEnabled = prefs.isLaunchOnStartupEnabled()
    val connectOnStart = prefs.isConnectOnLaunchEnabled()
    val connectOnUpdate = prefs.isConnectOnAppUpdateEnabled()
    val improvePiaEnabled = mutableStateOf(prefs.isHelpImprovePiaEnabled())
    val shadowsocksObfuscationEnabled = mutableStateOf(prefs.isShadowsocksObfuscationEnabled())
    val vpnExcludedApps = mutableStateOf(prefs.getVpnExcludedApps())
    val isAllowLocalTrafficEnabled = mutableStateOf(prefs.isAllowLocalTrafficEnabled())
    val appList = mutableStateOf<List<ApplicationInfo>>(emptyList())
    val eventList = mutableStateOf<List<String>>(emptyList())
    val debugLogs = mutableStateOf<List<String>>(emptyList())
    val requestId = mutableStateOf<String?>(null)
    val maceEnabled = mutableStateOf(prefs.isMaceEnabled())
    private val isDnsNumeric = mutableStateOf(true)
    private var installedApps = listOf<ApplicationInfo>()
    var reconnectDialogVisible = mutableStateOf(false)

    fun navigateUp() {
        when (_state.value) {
            SettingsStep.Automation -> _state.value = SettingsStep.Main
            SettingsStep.Obfuscation -> _state.value = SettingsStep.Main
            SettingsStep.ConnectionStats -> _state.value = SettingsStep.Help
            SettingsStep.DebugLogs -> _state.value = SettingsStep.Help
            SettingsStep.General -> _state.value = SettingsStep.Main
            SettingsStep.Help -> _state.value = SettingsStep.Main
            SettingsStep.KillSwitch -> _state.value = SettingsStep.Main
            SettingsStep.Main -> router.handleFlow(Back)
            SettingsStep.Network -> _state.value = SettingsStep.Main
            SettingsStep.Privacy -> _state.value = SettingsStep.Main
            SettingsStep.Protocol -> _state.value = SettingsStep.Main
            SettingsStep.ShortcutAutomation,
            SettingsStep.ShortcutKillSwitch,
            SettingsStep.ShortcutProtocol,
            -> router.handleFlow(Back)
        }
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

    fun navigateToObfuscationSettings() = viewModelScope.launch {
        _state.emit(SettingsStep.Obfuscation)
    }

    fun exitObfuscationSettings() = viewModelScope.launch {
        _state.emit(SettingsStep.Main)
    }

    fun navigateToKillSwitch() = viewModelScope.launch {
        _state.emit(SettingsStep.KillSwitch)
    }

    fun navigateToConnectionStats() = viewModelScope.launch {
        _state.emit(SettingsStep.ConnectionStats)
    }

    fun navigateToDebugLogs() = viewModelScope.launch {
        _state.emit(SettingsStep.DebugLogs)
    }

    fun navigateToAutomation() = router.handleFlow(EnterFlow.Automation)

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

        if (enable.not()) {
            toggleShadowsocksObfuscation(enabled = false)
        }
    }

    fun toggleShadowsocksObfuscation(enabled: Boolean) {
        prefs.setShadowsocksObfuscationEnabled(enabled)
        shadowsocksObfuscationEnabled.value = enabled
    }

    fun isPortForwardingEnabled() = prefs.isPortForwardingEnabled()

    fun getSelectedProtocol(): VpnProtocols = prefs.getSelectedProtocol()

    fun selectProtocol(protocol: VpnProtocols) {
        prefs.setSelectedProtocol(protocol)

        if (protocol != VpnProtocols.OpenVPN) {
            toggleShadowsocksObfuscation(enabled = false)
        }
    }

    fun getWireGuardSettings(): WireGuardSettings = prefs.getWireGuardSettings()

    fun getOpenVpnSettings(): OpenVpnSettings = prefs.getOpenVpnSettings()

    fun getCustomDns(): CustomDns = prefs.getCustomDns()

    fun setCustomDns(customDns: CustomDns) = prefs.setCustomDns(customDns = customDns)

    fun getCustomObfuscation(): CustomObfuscation? =
        prefs.getCustomObfuscation()

    fun setCustomObfuscation(customObfuscation: CustomObfuscation) =
        prefs.setCustomObfuscation(customObfuscation = customObfuscation)

    fun isNumericIpAddress(ipAddress: String): Boolean {
        isDnsNumeric.value = isNumericIpAddressUseCase.invoke(ipAddress = ipAddress)
        return isDnsNumeric.value
    }

    fun isPortValid(port: String): Boolean =
        port.isNotEmpty() && port.toInt() > 0

    fun setSelectedDnsOption(dnsOptions: DnsOptions) =
        prefs.setSelectedDnsOption(dnsOptions = dnsOptions)

    fun getSelectedDnsOption(): DnsOptions = prefs.getSelectedDnsOption()

    fun setSelectedObfuscationOption(obfuscationOptions: ObfuscationOptions) =
        prefs.setSelectedObfuscationOption(obfuscationOptions = obfuscationOptions)

    fun getSelectedObfuscationOption(): ObfuscationOptions = prefs.getSelectedObfuscationOption()

    fun isAutomationEnabled() = prefs.isAutomationEnabled()

    fun disableAutomation() = prefs.setAutomationEnabled(false)

    fun areLocationPermissionsGranted() =
        locationPermissionManager.isFineLocationPermissionGranted() &&
            locationPermissionManager.isBackgroundLocationPermissionGranted()

    fun setTransport(transport: Transport) {
        val currentSettings = getOpenVpnSettings()
        val hasSettingChanged = currentSettings.transport != transport
        currentSettings.transport = transport
        val ports = if (transport == Transport.UDP) {
            regionsRepository.getUdpPorts()
        } else {
            regionsRepository.getTcpPorts()
        }
        currentSettings.port = ports[0].toString()
        prefs.setOpenVpnSettings(currentSettings)

        if (hasSettingChanged) {
            showReconnectDialogIfVpnConnected()
        }

        if (transport == Transport.UDP) {
            toggleShadowsocksObfuscation(enabled = false)
        }
    }

    fun getTransport(): Transport =
        getOpenVpnSettings().transport

    fun setEncryption(encryption: DataEncryption) {
        val currentSettings = getOpenVpnSettings()
        val hasSettingChanged = currentSettings.dataEncryption != encryption
        currentSettings.dataEncryption = encryption
        prefs.setOpenVpnSettings(currentSettings)

        if (hasSettingChanged) {
            showReconnectDialogIfVpnConnected()
        }
    }

    fun setPort(port: String) {
        val currentSettings = getOpenVpnSettings()
        val hasSettingChanged = currentSettings.port != port
        currentSettings.port = port
        prefs.setOpenVpnSettings(currentSettings)

        if (hasSettingChanged) {
            showReconnectDialogIfVpnConnected()
        }
    }

    fun setOpenVpnEnableSmallPackets(enable: Boolean) {
        val currentSettings = getOpenVpnSettings()
        val hasSettingChanged = currentSettings.useSmallPackets != enable
        currentSettings.useSmallPackets = enable
        prefs.setOpenVpnSettings(currentSettings)

        if (hasSettingChanged) {
            showReconnectDialogIfVpnConnected()
        }
    }

    fun setWireGuardEnableSmallPackets(enable: Boolean) {
        val currentSettings = getWireGuardSettings()
        val hasSettingChanged = currentSettings.useSmallPackets != enable
        currentSettings.useSmallPackets = enable
        prefs.setWireGuardSettings(currentSettings)

        if (hasSettingChanged) {
            showReconnectDialogIfVpnConnected()
        }
    }

    fun getPorts(): Map<Int, String> {
        val availablePorts = mutableMapOf<Int, String>()
        when (getOpenVpnSettings().transport) {
            Transport.UDP -> {
                regionsRepository.getUdpPorts().distinct().forEach {
                    availablePorts[it] = it.toString()
                }
            }

            Transport.TCP -> {
                regionsRepository.getTcpPorts().distinct().forEach {
                    availablePorts[it] = it.toString()
                }
            }
        }
        return availablePorts
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
        appList.value = installedApps.sortedBy { packageManager.getApplicationLabel(it).toString() }
    }

    fun filterAppsByName(value: String, packageManager: PackageManager) {
        if (value.isEmpty()) {
            appList.value = installedApps
        } else {
            appList.value = installedApps.filter {
                packageManager.getApplicationLabel(it).toString().lowercase().contains(value.lowercase())
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

    fun showReconnectDialogIfVpnConnected() {
        if (isConnected()) {
            reconnectDialogVisible.value = true
        }
    }

    fun isConnected(): Boolean {
        return connectionUseCase.isConnected()
    }

    fun reconnect() {
        viewModelScope.launch {
            connectionPrefs.getSelectedVpnServer()?.let {
                connectionUseCase.reconnect(it).collect {}
            }
        }
    }
}