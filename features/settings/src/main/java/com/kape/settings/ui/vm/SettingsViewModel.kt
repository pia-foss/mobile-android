package com.kape.settings.ui.vm

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.AppInfo
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.KpiDataSource
import com.kape.contracts.Router
import com.kape.csi.domain.SendLogUseCase
import com.kape.data.About
import com.kape.data.AutomationSettings
import com.kape.data.ConnectionStats
import com.kape.data.DebugLogs
import com.kape.data.ExternalAppList
import com.kape.data.GeneralSettings
import com.kape.data.HelpSettings
import com.kape.data.KillSwitchSettings
import com.kape.data.NetworkSettings
import com.kape.data.ObfuscationSettings
import com.kape.data.PrivacySettings
import com.kape.data.ProtocolSettings
import com.kape.data.WebDestination
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.location.data.LocationPermissionManager
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
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.GetLogsUseCase
import com.kape.vpnregions.data.VpnRegionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SettingsViewModel(
    private val router: Router,
    private val appInfo: AppInfo,
    private val prefs: SettingsPrefs,
    private val connectionPrefs: ConnectionPrefs,
    private val csiPrefs: CsiPrefs,
    private val regionsRepository: VpnRegionRepository,
    private val kpiDataSource: KpiDataSource,
    private val connectionDataSource: ConnectionDataSource,
    private val getDebugLogsUseCase: GetLogsUseCase,
    private val sendLogUseCase: SendLogUseCase,
    private val isNumericIpAddressUseCase: IsNumericIpAddressUseCase,
    private val locationPermissionManager: LocationPermissionManager,
    private val connectionManager: ConnectionManager,
    private val connectionInfoProvider: ConnectionInfoProvider,
) : ViewModel() {
    companion object {
        private const val GET_INSTALLED_APPS_DELAY_MS = 1000L
    }

    fun launchOnBootEnabled() = prefs.isLaunchOnStartupEnabled

    fun connectOnStart() = prefs.isConnectOnLaunchEnabled

    fun connectOnUpdate() = prefs.isConnectOnAppUpdateEnabled

    fun showGeoLocatedServers() = prefs.isShowGeoLocatedServersEnabled

    fun improvePiaEnabled() = prefs.isHelpImprovePiaEnabled

    fun debugLoggingEnabled() = prefs.isDebugLoggingEnabled

    fun shadowsocksObfuscationEnabled() = prefs.isShadowsocksObfuscationEnabled

    fun vpnExcludedApps() = prefs.vpnExcludedApps

    fun isAllowLocalTrafficEnabled() = prefs.isAllowLocalTrafficEnabled

    fun externalProxyAppEnabled() = prefs.isExternalProxyAppEnabled

    fun externalProxyAppPackageName() = prefs.externalProxyAppPackageName

    fun externalProxyAppPort() = connectionPrefs.proxyPort

    val appList = mutableStateOf<List<ApplicationInfo>>(emptyList())
    val eventList = mutableStateOf<List<String>>(emptyList())
    val debugLogs = mutableStateOf<List<String>>(emptyList())
    val requestId = mutableStateOf<String?>(null)
    val selectedProtocol = prefs.selectedProtocol

    fun maceEnabled() = prefs.isMaceEnabled

    private val isDnsNumeric = mutableStateOf(true)
    private var installedApps = listOf<ApplicationInfo>()
    var reconnectDialogVisible = mutableStateOf(false)
    val externalProxyTcpDialogVisible = mutableStateOf(false)

    fun navigateToGeneralSettings() = router.updateDestination(GeneralSettings)

    fun navigateToProtocolSettings() = router.updateDestination(ProtocolSettings)

    fun navigateToNetworkSettings() = router.updateDestination(NetworkSettings)

    fun navigateToPrivacySettings() = router.updateDestination(PrivacySettings)

    fun navigateToHelpSettings() = router.updateDestination(HelpSettings)

    fun navigateToAutomation() = router.updateDestination(AutomationSettings)

    fun navigateToObfuscationSettings() = router.updateDestination(ObfuscationSettings)

    fun navigateToKillSwitch() = router.updateDestination(KillSwitchSettings)

    fun navigateToConnectionStats() = router.updateDestination(ConnectionStats)

    fun navigateToDebugLogs() = router.updateDestination(DebugLogs)

    fun navigateToPrivacyPolicy() = router.updateDestination(WebDestination.Privacy)

    fun navigateToAbout() = router.updateDestination(About)

    fun navigateToExternalAppList() = router.updateDestination(ExternalAppList)

    fun navigateBack() = router.navigateBack()

    fun isAutomationEnabled() = prefs.isAutomationEnabled

    fun toggleLaunchOnBoot(enable: Boolean) {
        prefs.setEnableLaunchOnStartup(enable)
    }

    fun toggleConnectOnStart(enable: Boolean) {
        prefs.setEnableConnectOnLaunch(enable)
    }

    fun toggleConnectOnUpdate(enable: Boolean) {
        prefs.setEnableConnectOnAppUpdate(enable)
    }

    fun toggleShowGeoLocatedServers(enable: Boolean) {
        prefs.setEnabledShowGeoLocatedServers(enable)
    }

    fun toggleImprovePia(enable: Boolean) {
        prefs.setHelpImprovePiaEnabled(enable)
    }

    fun toggleDebugLogging(enable: Boolean) {
        prefs.setDebugLoggingEnabled(enable)
        if (enable.not()) {
            csiPrefs.clearCustomDebugLogs()
        }
    }

    fun toggleMace(enable: Boolean) {
        prefs.setMaceEnabled(enable)
    }

    fun toggleEnablePortForwarding(enable: Boolean) {
        prefs.setEnablePortForwarding(enable)
    }

    fun toggleAllowLocalNetwork(enable: Boolean) {
        prefs.setAllowLocalTrafficEnabled(enable)

        if (enable.not()) {
            toggleShadowsocksObfuscation(enabled = false)
        }
    }

    fun toggleShadowsocksObfuscation(enabled: Boolean) {
        prefs.setShadowsocksObfuscationEnabled(enabled)
    }

    fun toggleExternalProxyApp(enabled: Boolean) {
        prefs.setExternalProxyAppEnabled(enabled)
    }

    fun setExternalProxyAppPackageName(packageName: String) {
        if (packageName.isEmpty()) {
            prefs.setExternalProxyAppPackageName(packageName)
            removeFromVpnExcludedApps(externalProxyAppPackageName().value)
        } else {
            prefs.setExternalProxyAppPackageName(packageName)
            addToVpnExcludedApps(packageName)
        }
        toggleExternalProxyApp(packageName.isNotEmpty())
    }

    fun setExternalProxyPort(port: String?) {
        connectionPrefs.setProxyPort(port)
    }

    fun isPortForwardingEnabled() = prefs.isPortForwardingEnabled

    fun selectProtocol(protocol: VpnProtocols) {
        prefs.setSelectedProtocol(protocol)

        if (protocol != VpnProtocols.OpenVPN) {
            toggleShadowsocksObfuscation(enabled = false)
        }
    }

    fun getWireGuardSettings(): WireGuardSettings = prefs.wireGuardSettings.value

    fun getOpenVpnSettings(): OpenVpnSettings = prefs.openVpnSettings.value

    fun getCustomDns(): CustomDns = prefs.customDns.value

    fun setCustomDns(customDns: CustomDns) = prefs.setCustomDns(customDns = customDns)

    fun getCustomObfuscation(): CustomObfuscation? = prefs.customObfuscation.value

    fun setCustomObfuscation(customObfuscation: CustomObfuscation) = prefs.setCustomObfuscation(customObfuscation = customObfuscation)

    fun isNumericIpAddress(ipAddress: String): Boolean {
        isDnsNumeric.value = isNumericIpAddressUseCase.invoke(ipAddress = ipAddress)
        return isDnsNumeric.value
    }

    fun isPortValid(port: String): Boolean = port.isNotEmpty() && port.toInt() > 0

    fun setSelectedDnsOption(dnsOptions: DnsOptions) = prefs.setSelectedDnsOption(dnsOptions = dnsOptions)

    fun getSelectedDnsOption(): DnsOptions = prefs.selectedDnsOption.value

    fun setSelectedObfuscationOption(obfuscationOptions: ObfuscationOptions) =
        prefs.setSelectedObfuscationOption(obfuscationOptions = obfuscationOptions)

    fun getSelectedObfuscationOption(): ObfuscationOptions = prefs.selectedObfuscationOption.value

    fun setTransport(transport: Transport) {
        val currentSettings = getOpenVpnSettings()
        val hasSettingChanged = currentSettings.transport != transport
        currentSettings.transport = transport
        val ports =
            if (transport == Transport.UDP) {
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

    fun getTransport(): Transport = getOpenVpnSettings().transport

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
        newList.addAll(prefs.vpnExcludedApps.value)
        newList.add(app)
        prefs.setVpnExcludedApps(newList)
    }

    fun removeFromVpnExcludedApps(app: String) {
        val newList = mutableListOf<String>()
        newList.addAll(prefs.vpnExcludedApps.value)
        newList.remove(app)
        prefs.setVpnExcludedApps(newList)
    }

    fun getInstalledApplications(packageManager: PackageManager) =
        viewModelScope.launch(Dispatchers.IO) {
            // Allow for the transition to take effect. Otherwise the transition and the loading of the
            // packages will cause a stutter due to the recomposition and the transition happening
            // at around the same time.
            delay(GET_INSTALLED_APPS_DELAY_MS)

            installedApps =
                PerAppSettingsUtils
                    .getInstalledApps(packageManager = packageManager)
                    .filterNotNull()
            val sortedApps =
                installedApps.sortedBy { packageManager.getApplicationLabel(it).toString() }
            withContext(Dispatchers.Main) {
                appList.value = sortedApps
            }
        }

    fun filterAppsByName(
        value: String,
        packageManager: PackageManager,
    ) {
        if (value.isEmpty()) {
            appList.value =
                installedApps.sortedBy { packageManager.getApplicationLabel(it).toString() }
        } else {
            appList.value =
                installedApps.filter {
                    packageManager
                        .getApplicationLabel(it)
                        .toString()
                        .lowercase()
                        .contains(value.lowercase())
                }
        }
    }

    fun getRecentEvents() =
        viewModelScope.launch {
            eventList.value = kpiDataSource.recentEvents()
        }

    fun getDebugLogs() =
        viewModelScope.launch {
            debugLogs.value = getDebugLogsUseCase.getDebugLogs()
        }

    fun sendLogs() =
        viewModelScope.launch {
            val logs = connectionDataSource.getDebugLogs()
            csiPrefs.setProtocolDebugLogs(logs.joinToString(separator = "\n"))
            val result = sendLogUseCase.sendLog()
            requestId.value = result
            csiPrefs.clearCustomDebugLogs()
        }

    fun resetRequestId() {
        requestId.value = null
    }

    fun showReconnectDialogIfVpnConnected() {
        if (connectionInfoProvider.isConnected()) {
            reconnectDialogVisible.value = true
        }
    }

    fun showExternalProxyTcpDialogIfNeeded() {
        if (prefs.isExternalProxyAppEnabled.value && prefs.openVpnSettings.value.transport != Transport.TCP) {
            externalProxyTcpDialogVisible.value = true
        }
    }

    fun reconnect() {
        connectionPrefs.selectedVpnServer.value?.let {
            connectionManager.connectJob =
                viewModelScope.launch {
                    if (connectionManager.isConnectionInProgress()) {
                        connectionManager.disconnect().getOrNull()
                    }
                    connectionManager.connect(
                        it,
                        true,
                        ::callback,
                        {
                            // no-op for now, might be used for fallback
                        },
                    )
                }
        }
    }

    private fun callback() {
        viewModelScope.launch { connectionManager.disconnect().getOrNull() }
    }

    fun isConnected() = connectionInfoProvider.isConnected()

    fun getAppVersion(): String = "${appInfo.versionName} (${appInfo.versionCode})"
}