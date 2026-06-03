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
import com.kape.data.DI
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
import com.kape.settings.data.CustomDns
import com.kape.settings.data.CustomObfuscation
import com.kape.settings.data.DataEncryption
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.settings.domain.IsNumericIpAddressUseCase
import com.kape.settings.utils.PerAppSettingsUtils
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.GetLogsUseCase
import com.kape.vpnregions.data.VpnRegionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

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
    private val connectionManager: ConnectionManager,
    private val connectionInfoProvider: ConnectionInfoProvider,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    companion object {
        private const val GET_INSTALLED_APPS_DELAY_MS = 1000L
    }

    val launchOnBootEnabled = prefs.isLaunchOnStartupEnabled
    val connectOnStart = prefs.isConnectOnLaunchEnabled
    val connectOnUpdate = prefs.isConnectOnAppUpdateEnabled
    val showGeoLocatedServers = prefs.isShowGeoLocatedServersEnabled
    val improvePiaEnabled = prefs.isHelpImprovePiaEnabled
    val debugLoggingEnabled = prefs.isDebugLoggingEnabled
    val shadowsocksObfuscationEnabled = prefs.isShadowsocksObfuscationEnabled
    val vpnExcludedApps = prefs.vpnExcludedApps
    val isAllowLocalTrafficEnabled = prefs.isAllowLocalTrafficEnabled
    val externalProxyAppEnabled = prefs.isExternalProxyAppEnabled
    val externalProxyAppPackageName = prefs.externalProxyAppPackageName
    val externalProxyAppPort = connectionPrefs.proxyPort
    val selectedDnsOption = prefs.selectedDnsOption
    val appList = mutableStateOf<List<ApplicationInfo>>(emptyList())
    val eventList = mutableStateOf<List<String>>(emptyList())
    val debugLogs = mutableStateOf<List<String>>(emptyList())
    val requestId = mutableStateOf<String?>(null)
    val selectedProtocol = prefs.selectedProtocol
    val maceEnabled = prefs.isMaceEnabled
    val wireGuardSettings = prefs.wireGuardSettings
    val openVpnSettings = prefs.openVpnSettings

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

    fun toggleLaunchOnBoot(enabled: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setEnableLaunchOnStartup(enabled)
        }

    fun toggleConnectOnStart(enabled: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setEnableConnectOnLaunch(enabled)
        }

    fun toggleConnectOnUpdate(enabled: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setEnableConnectOnAppUpdate(enabled)
        }

    fun toggleShowGeoLocatedServers(enabled: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setEnabledShowGeoLocatedServers(enabled)
        }

    fun toggleImprovePia(enabled: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setHelpImprovePiaEnabled(enabled)
        }

    fun toggleDebugLogging(enabled: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setDebugLoggingEnabled(enabled)
            if (enabled.not()) {
                csiPrefs.clearCustomDebugLogs()
            }
        }

    fun toggleMace(enabled: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setMaceEnabled(enabled)
        }

    fun toggleEnablePortForwarding(enabled: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setEnablePortForwarding(enabled)
        }

    fun toggleAllowLocalNetwork(enabled: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setAllowLocalTrafficEnabled(enabled)

            if (enabled.not()) {
                toggleShadowsocksObfuscation(enabled = false)
            }
        }

    fun toggleShadowsocksObfuscation(enabled: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setShadowsocksObfuscationEnabled(enabled)
        }

    fun toggleExternalProxyApp(enabled: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setExternalProxyAppEnabled(enabled)
        }

    fun setExternalProxyAppPackageName(packageName: String) =
        viewModelScope.launch(ioDispatcher) {
            if (packageName.isEmpty()) {
                prefs.setExternalProxyAppPackageName(packageName)
                removeFromVpnExcludedApps(externalProxyAppPackageName.value)
            } else {
                prefs.setExternalProxyAppPackageName(packageName)
                addToVpnExcludedApps(packageName)
            }
            toggleExternalProxyApp(packageName.isNotEmpty())
        }

    fun setExternalProxyPort(port: String?) =
        viewModelScope.launch(ioDispatcher) {
            connectionPrefs.setProxyPort(port)
        }

    fun isPortForwardingEnabled() = prefs.isPortForwardingEnabled

    fun selectProtocol(protocol: VpnProtocols) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setSelectedProtocol(protocol)

            if (protocol != VpnProtocols.OpenVPN) {
                toggleShadowsocksObfuscation(enabled = false)
            }
        }

    val customDns = prefs.customDns

    fun setCustomDns(customDns: CustomDns) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setCustomDns(customDns = customDns)
        }

    val customObfuscation = prefs.customObfuscation

    fun setCustomObfuscation(customObfuscation: CustomObfuscation) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setCustomObfuscation(customObfuscation = customObfuscation)
        }

    fun isNumericIpAddress(ipAddress: String): Boolean {
        isDnsNumeric.value = isNumericIpAddressUseCase.invoke(ipAddress = ipAddress)
        return isDnsNumeric.value
    }

    fun isPortValid(port: String): Boolean = port.isNotEmpty() && port.toInt() > 0

    fun setSelectedDnsOption(dnsOptions: DnsOptions) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setSelectedDnsOption(dnsOptions = dnsOptions)
        }

    fun setSelectedObfuscationOption(obfuscationOptions: ObfuscationOptions) =
        viewModelScope.launch(ioDispatcher) {
            prefs.setSelectedObfuscationOption(obfuscationOptions = obfuscationOptions)
        }

    fun getSelectedObfuscationOption(): ObfuscationOptions = prefs.selectedObfuscationOption.value

    fun setTransport(transport: Transport) {
        viewModelScope.launch(ioDispatcher) {
            val currentSettings = openVpnSettings.first()
            val hasSettingChanged = currentSettings.transport != transport
            val ports =
                if (transport == Transport.UDP) {
                    regionsRepository.getUdpPorts()
                } else {
                    regionsRepository.getTcpPorts()
                }
            prefs.setOpenVpnSettings(currentSettings.copy(transport = transport, port = ports[0].toString()))

            if (hasSettingChanged) {
                showReconnectDialogIfVpnConnected()
            }

            if (transport == Transport.UDP) {
                toggleShadowsocksObfuscation(enabled = false)
            }
        }
    }

    fun getTransport(): Transport = openVpnSettings.value.transport

    fun setEncryption(encryption: DataEncryption) {
        viewModelScope.launch(ioDispatcher) {
            val currentSettings = openVpnSettings.first()
            val hasSettingChanged = currentSettings.dataEncryption != encryption
            prefs.setOpenVpnSettings(currentSettings.copy(dataEncryption = encryption))

            if (hasSettingChanged) {
                showReconnectDialogIfVpnConnected()
            }
        }
    }

    fun setPort(port: String) {
        viewModelScope.launch(ioDispatcher) {
            val currentSettings = openVpnSettings.first()
            val hasSettingChanged = currentSettings.port != port
            prefs.setOpenVpnSettings(currentSettings.copy(port = port))

            if (hasSettingChanged) {
                showReconnectDialogIfVpnConnected()
            }
        }
    }

    fun setOpenVpnEnableSmallPackets(enabled: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            val currentSettings = openVpnSettings.first()
            val hasSettingChanged = currentSettings.useSmallPackets != enabled
            prefs.setOpenVpnSettings(currentSettings.copy(useSmallPackets = enabled))

            if (hasSettingChanged) {
                showReconnectDialogIfVpnConnected()
            }
        }
    }

    fun setWireGuardEnableSmallPackets(enabled: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            val currentSettings = wireGuardSettings.first()
            val hasSettingChanged = currentSettings.useSmallPackets != enabled
            prefs.setWireGuardSettings(currentSettings.copy(useSmallPackets = enabled))

            if (hasSettingChanged) {
                showReconnectDialogIfVpnConnected()
            }
        }
    }

    fun getPorts(): Map<Int, String> {
        val availablePorts = mutableMapOf<Int, String>()
        when (openVpnSettings.value.transport) {
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
        viewModelScope.launch(ioDispatcher) {
            val newList = mutableListOf<String>()
            newList.addAll(prefs.vpnExcludedApps.value)
            newList.add(app)
            prefs.setVpnExcludedApps(newList)
        }
    }

    fun removeFromVpnExcludedApps(app: String) {
        viewModelScope.launch(ioDispatcher) {
            val newList = mutableListOf<String>()
            newList.addAll(prefs.vpnExcludedApps.value)
            newList.remove(app)
            prefs.setVpnExcludedApps(newList)
        }
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
        viewModelScope.launch(ioDispatcher) {
            eventList.value = kpiDataSource.recentEvents()
        }

    fun getDebugLogs() =
        viewModelScope.launch(ioDispatcher) {
            debugLogs.value = getDebugLogsUseCase.getDebugLogs()
        }

    fun sendLogs() =
        viewModelScope.launch(ioDispatcher) {
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
                viewModelScope.launch(ioDispatcher) {
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
        viewModelScope.launch(ioDispatcher) { connectionManager.disconnect().getOrNull() }
    }

    fun isConnected() = connectionInfoProvider.isConnected()

    fun getAppVersion(): String = "${appInfo.versionName} (${appInfo.versionCode})"
}