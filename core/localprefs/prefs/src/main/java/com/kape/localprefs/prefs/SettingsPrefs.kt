package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.kape.localprefs.Prefs
import com.kape.settings.data.CustomDns
import com.kape.settings.data.CustomObfuscation
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

private val LAUNCH_ON_STARTUP = booleanPreferencesKey("launch-on-startup")
private val CONNECT_ON_LAUNCH = booleanPreferencesKey("connect-on-launch")
private val CONNECT_ON_APP_UPDATE = booleanPreferencesKey("connect-on-app-update")
private val SHOW_GEO_SERVERS = booleanPreferencesKey("show-geo-located-servers")
private val SELECTED_PROTOCOL = stringPreferencesKey("selected-protocol")
private val WIRE_GUARD_SETTINGS = stringPreferencesKey("wireguard-settings")
private val OPEN_VPN_SETTINGS = stringPreferencesKey("openvpn-settings")
private val SELECTED_DNS_OPTION_SETTINGS = stringPreferencesKey("selected-dns-option-settings")
private val CUSTOM_DNS_SETTINGS = stringPreferencesKey("custom-dns-settings")
private val SELECTED_OBFUSCATION_OPTION_SETTINGS = stringPreferencesKey("selected-obfuscation-option-settings")
private val CUSTOM_OBFUSCATION_SETTINGS = stringPreferencesKey("custom-obfuscation-settings")
private val SHADOWSOCKS_OBFUSCATION_ENABLED = booleanPreferencesKey("shadowsocks-obfuscation-enabled")
private val EXTERNAL_PROXY_APP_ENABLED = booleanPreferencesKey("external-proxy-app-enabled")
private val EXTERNAL_PROXY_APP_PACKAGE_NAME = stringPreferencesKey("external-proxy-app-package-name")
private val HELP_IMPROVE_PIA = booleanPreferencesKey("help-improve-pia")
private val DEBUG_LOGGING = booleanPreferencesKey("debug-logging")
private val VPN_EXCLUDED_APPS = stringSetPreferencesKey("vpn-excluded-apps")
private val PORT_FORWARDING = booleanPreferencesKey("port-forwarding")
private val ALLOW_LOCAL_TRAFFIC = booleanPreferencesKey("allow-local-traffic")
private val AUTOMATION = booleanPreferencesKey("setting-automation")
private val MACE = booleanPreferencesKey("setting-mace")

@Singleton
class SettingsPrefs(
    context: Context,
) : Prefs(context, "settings") {
    val isLaunchOnStartupEnabled: StateFlow<Boolean> =
        getLaunchOnStartupEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    val isConnectOnLaunchEnabled: StateFlow<Boolean> =
        getConnectOnLaunchEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    val isConnectOnAppUpdateEnabled: StateFlow<Boolean> =
        getConnectOnAppUpdateEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    val isShowGeoLocatedServersEnabled: StateFlow<Boolean> =
        getShowGeoLocatedServersEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), true)
    val selectedProtocol: StateFlow<VpnProtocols> =
        getSelectedProtocol().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), VpnProtocols.WireGuard)
    val wireGuardSettings: StateFlow<WireGuardSettings> =
        getWireGuardSettings().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), WireGuardSettings())
    val openVpnSettings: StateFlow<OpenVpnSettings> =
        getOpenVpnSettings().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), OpenVpnSettings())
    val selectedDnsOption: StateFlow<DnsOptions> =
        getSelectedDnsOption().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), DnsOptions.PIA)
    val customDns: StateFlow<CustomDns> =
        getCustomDns().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), CustomDns())
    val isShadowsocksObfuscationEnabled: StateFlow<Boolean> =
        getShadowsocksObfuscationEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    val isExternalProxyAppEnabled: StateFlow<Boolean> =
        getExternalProxyAppEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    val externalProxyAppPackageName: StateFlow<String> =
        getExternalProxyAppPackageName().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), "")
    val selectedObfuscationOption: StateFlow<ObfuscationOptions> =
        getSelectedObfuscationOption().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), ObfuscationOptions.PIA)
    val customObfuscation: StateFlow<CustomObfuscation?> =
        getCustomObfuscation().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), null)
    val isHelpImprovePiaEnabled: StateFlow<Boolean> =
        getHelpImprovePiaEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    val isDebugLoggingEnabled: StateFlow<Boolean> =
        getDebugLoggingEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    val vpnExcludedApps: StateFlow<List<String>> =
        getVpnExcludedApps().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), emptyList())
    val isPortForwardingEnabled: StateFlow<Boolean> =
        getPortForwardingEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    val isAllowLocalTrafficEnabled: StateFlow<Boolean> =
        getAllowLocalTrafficEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    val isAutomationEnabled: StateFlow<Boolean> =
        getAutomationEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    val isMaceEnabled: StateFlow<Boolean> =
        getMaceEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)

    suspend fun setEnableLaunchOnStartup(enabled: Boolean) {
        dataStore.edit { it[LAUNCH_ON_STARTUP] = enabled }
    }

    suspend fun setEnableConnectOnLaunch(enabled: Boolean) {
        dataStore.edit { it[CONNECT_ON_LAUNCH] = enabled }
    }

    suspend fun setEnableConnectOnAppUpdate(enabled: Boolean) {
        dataStore.edit { it[CONNECT_ON_APP_UPDATE] = enabled }
    }

    suspend fun setEnabledShowGeoLocatedServers(enabled: Boolean) {
        dataStore.edit { it[SHOW_GEO_SERVERS] = enabled }
    }

    suspend fun setSelectedProtocol(protocol: VpnProtocols) {
        dataStore.edit { it[SELECTED_PROTOCOL] = Json.encodeToString(protocol) }
    }

    suspend fun setWireGuardSettings(settings: WireGuardSettings) {
        dataStore.edit { it[WIRE_GUARD_SETTINGS] = Json.encodeToString(settings) }
    }

    suspend fun setOpenVpnSettings(settings: OpenVpnSettings) {
        dataStore.edit { it[OPEN_VPN_SETTINGS] = Json.encodeToString(settings) }
    }

    suspend fun setSelectedDnsOption(dnsOptions: DnsOptions) {
        dataStore.edit { it[SELECTED_DNS_OPTION_SETTINGS] = Json.encodeToString(dnsOptions) }
    }

    suspend fun setCustomDns(customDns: CustomDns) {
        dataStore.edit { it[CUSTOM_DNS_SETTINGS] = Json.encodeToString(customDns) }
    }

    suspend fun setShadowsocksObfuscationEnabled(enabled: Boolean) {
        dataStore.edit { it[SHADOWSOCKS_OBFUSCATION_ENABLED] = enabled }
    }

    suspend fun setExternalProxyAppEnabled(enabled: Boolean) {
        dataStore.edit { it[EXTERNAL_PROXY_APP_ENABLED] = enabled }
    }

    suspend fun setExternalProxyAppPackageName(packageName: String) {
        dataStore.edit { it[EXTERNAL_PROXY_APP_PACKAGE_NAME] = packageName }
    }

    suspend fun setSelectedObfuscationOption(obfuscationOptions: ObfuscationOptions) {
        dataStore.edit { it[SELECTED_OBFUSCATION_OPTION_SETTINGS] = Json.encodeToString(obfuscationOptions) }
    }

    suspend fun setCustomObfuscation(customObfuscation: CustomObfuscation) {
        dataStore.edit { it[CUSTOM_OBFUSCATION_SETTINGS] = Json.encodeToString(customObfuscation) }
    }

    suspend fun setHelpImprovePiaEnabled(enabled: Boolean) {
        dataStore.edit { it[HELP_IMPROVE_PIA] = enabled }
    }

    suspend fun setDebugLoggingEnabled(enabled: Boolean) {
        dataStore.edit { it[DEBUG_LOGGING] = enabled }
    }

    suspend fun setVpnExcludedApps(apps: List<String>) {
        dataStore.edit { it[VPN_EXCLUDED_APPS] = apps.toSet() }
    }

    suspend fun setEnablePortForwarding(enabled: Boolean) {
        dataStore.edit { it[PORT_FORWARDING] = enabled }
    }

    suspend fun setAllowLocalTrafficEnabled(enabled: Boolean) {
        dataStore.edit { it[ALLOW_LOCAL_TRAFFIC] = enabled }
    }

    suspend fun setAutomationEnabled(enabled: Boolean) {
        dataStore.edit { it[AUTOMATION] = enabled }
    }

    suspend fun setMaceEnabled(enabled: Boolean) {
        dataStore.edit { it[MACE] = enabled }
    }

    private fun getLaunchOnStartupEnabled(): Flow<Boolean> = dataStore.data.map { it[LAUNCH_ON_STARTUP] ?: false }

    private fun getConnectOnLaunchEnabled(): Flow<Boolean> = dataStore.data.map { it[CONNECT_ON_LAUNCH] ?: false }

    private fun getConnectOnAppUpdateEnabled(): Flow<Boolean> = dataStore.data.map { it[CONNECT_ON_APP_UPDATE] ?: false }

    private fun getShowGeoLocatedServersEnabled(): Flow<Boolean> = dataStore.data.map { it[SHOW_GEO_SERVERS] ?: true }

    private fun getSelectedProtocol(): Flow<VpnProtocols> =
        dataStore.data.map { prefs ->
            prefs[SELECTED_PROTOCOL]?.let { Json.decodeFromString(it) } ?: VpnProtocols.WireGuard
        }

    private fun getWireGuardSettings(): Flow<WireGuardSettings> =
        dataStore.data.map { prefs ->
            prefs[WIRE_GUARD_SETTINGS]?.let { Json.decodeFromString(it) } ?: WireGuardSettings()
        }

    private fun getOpenVpnSettings(): Flow<OpenVpnSettings> =
        dataStore.data.map { prefs ->
            prefs[OPEN_VPN_SETTINGS]?.let { Json.decodeFromString(it) } ?: OpenVpnSettings()
        }

    private fun getSelectedDnsOption(): Flow<DnsOptions> =
        dataStore.data.map { prefs ->
            prefs[SELECTED_DNS_OPTION_SETTINGS]?.let { Json.decodeFromString(it) } ?: DnsOptions.PIA
        }

    private fun getCustomDns(): Flow<CustomDns> =
        dataStore.data.map { prefs ->
            prefs[CUSTOM_DNS_SETTINGS]?.let { Json.decodeFromString(it) } ?: CustomDns()
        }

    private fun getShadowsocksObfuscationEnabled(): Flow<Boolean> = dataStore.data.map { it[SHADOWSOCKS_OBFUSCATION_ENABLED] ?: false }

    private fun getExternalProxyAppEnabled(): Flow<Boolean> = dataStore.data.map { it[EXTERNAL_PROXY_APP_ENABLED] ?: false }

    private fun getExternalProxyAppPackageName(): Flow<String> = dataStore.data.map { it[EXTERNAL_PROXY_APP_PACKAGE_NAME] ?: "" }

    private fun getSelectedObfuscationOption(): Flow<ObfuscationOptions> =
        dataStore.data.map { prefs ->
            prefs[SELECTED_OBFUSCATION_OPTION_SETTINGS]?.let { Json.decodeFromString(it) } ?: ObfuscationOptions.PIA
        }

    private fun getCustomObfuscation(): Flow<CustomObfuscation?> =
        dataStore.data.map { prefs ->
            prefs[CUSTOM_OBFUSCATION_SETTINGS]?.let { Json.decodeFromString(it) }
        }

    private fun getHelpImprovePiaEnabled(): Flow<Boolean> = dataStore.data.map { it[HELP_IMPROVE_PIA] ?: false }

    private fun getDebugLoggingEnabled(): Flow<Boolean> = dataStore.data.map { it[DEBUG_LOGGING] ?: false }

    private fun getVpnExcludedApps(): Flow<List<String>> = dataStore.data.map { it[VPN_EXCLUDED_APPS]?.toList() ?: emptyList() }

    private fun getPortForwardingEnabled(): Flow<Boolean> = dataStore.data.map { it[PORT_FORWARDING] ?: false }

    private fun getAllowLocalTrafficEnabled(): Flow<Boolean> = dataStore.data.map { it[ALLOW_LOCAL_TRAFFIC] ?: false }

    private fun getAutomationEnabled(): Flow<Boolean> = dataStore.data.map { it[AUTOMATION] ?: false }

    private fun getMaceEnabled(): Flow<Boolean> = dataStore.data.map { it[MACE] ?: false }
}