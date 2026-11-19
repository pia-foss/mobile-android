package com.kape.localprefs.api

import com.kape.localprefs.data.settings.AutomaticSettings
import com.kape.settings.data.CustomDns
import com.kape.settings.data.CustomObfuscation
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import kotlinx.coroutines.flow.StateFlow

interface SettingsStore {
    val autoSettings: StateFlow<AutomaticSettings>

    val customDns: StateFlow<CustomDns>

    val customObfuscation: StateFlow<CustomObfuscation?>

    val externalProxyAppPackageName: StateFlow<String>

    suspend fun getAutoSettingsNow(): AutomaticSettings

    suspend fun getExternalProxyAppPackageNameNow(): String

    suspend fun getOpenVpnSettingsNow(): OpenVpnSettings

    suspend fun getSelectedProtocolNow(): VpnProtocols

    suspend fun getVpnExcludedAppsNow(): List<String>

    suspend fun getWireGuardSettingsNow(): WireGuardSettings

    val isAllowLocalTrafficEnabled: StateFlow<Boolean>

    val isAutomationEnabled: StateFlow<Boolean>

    suspend fun isAutomationEnabledNow(): Boolean

    val isConnectOnAppUpdateEnabled: StateFlow<Boolean>

    val isConnectOnLaunchEnabled: StateFlow<Boolean>

    suspend fun isConnectOnLaunchEnabledNow(): Boolean

    val isDebugLoggingEnabled: StateFlow<Boolean>

    val isExternalProxyAppEnabled: StateFlow<Boolean>

    val isLaunchOnStartupEnabled: StateFlow<Boolean>

    val isMaceEnabled: StateFlow<Boolean>

    val isMockLocationEnabled: StateFlow<Boolean>

    val isPortForwardingEnabled: StateFlow<Boolean>

    val isShadowsocksObfuscationEnabled: StateFlow<Boolean>

    val isShowGeoLocatedServersEnabled: StateFlow<Boolean>

    val openVpnSettings: StateFlow<OpenVpnSettings>

    val selectedDnsOption: StateFlow<DnsOptions>

    val selectedObfuscationOption: StateFlow<ObfuscationOptions>

    val selectedProtocol: StateFlow<VpnProtocols>

    suspend fun setAllowLocalTrafficEnabled(enabled: Boolean)

    suspend fun setAutoSettings(settings: AutomaticSettings)

    suspend fun setAutomationEnabled(enabled: Boolean)

    suspend fun setCustomDns(customDns: CustomDns)

    suspend fun setCustomObfuscation(customObfuscation: CustomObfuscation)

    suspend fun setDebugLoggingEnabled(enabled: Boolean)

    suspend fun setEnableConnectOnAppUpdate(enabled: Boolean)

    suspend fun setEnableConnectOnLaunch(enabled: Boolean)

    suspend fun setEnableLaunchOnStartup(enabled: Boolean)

    suspend fun setEnablePortForwarding(enabled: Boolean)

    suspend fun setEnabledShowGeoLocatedServers(enabled: Boolean)

    suspend fun setExternalProxyAppEnabled(enabled: Boolean)

    suspend fun setExternalProxyAppPackageName(packageName: String)

    suspend fun setMaceEnabled(enabled: Boolean)

    suspend fun setMockLocationEnabled(enabled: Boolean)

    suspend fun setOpenVpnSettings(settings: OpenVpnSettings)

    suspend fun setSelectedDnsOption(dnsOptions: DnsOptions)

    suspend fun setSelectedObfuscationOption(obfuscationOptions: ObfuscationOptions)

    suspend fun setSelectedProtocol(protocol: VpnProtocols)

    suspend fun setShadowsocksObfuscationEnabled(enabled: Boolean)

    suspend fun setVpnExcludedApps(apps: List<String>)

    suspend fun setWireGuardSettings(settings: WireGuardSettings)

    val vpnExcludedApps: StateFlow<List<String>>

    val wireGuardSettings: StateFlow<WireGuardSettings>
}