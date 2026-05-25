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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
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
    fun setEnableLaunchOnStartup(enable: Boolean) {
        scope.launch {
            dataStore.edit { it[LAUNCH_ON_STARTUP] = enable }
        }
    }

    fun isLaunchOnStartupEnabled(): Flow<Boolean> = dataStore.data.map { it[LAUNCH_ON_STARTUP] ?: false }

    fun setEnableConnectOnLaunch(enable: Boolean) {
        scope.launch {
            dataStore.edit { it[CONNECT_ON_LAUNCH] = enable }
        }
    }

    fun isConnectOnLaunchEnabled(): Flow<Boolean> = dataStore.data.map { it[CONNECT_ON_LAUNCH] ?: false }

    fun setEnableConnectOnAppUpdate(enable: Boolean) {
        scope.launch {
            dataStore.edit { it[CONNECT_ON_APP_UPDATE] = enable }
        }
    }

    fun isConnectOnAppUpdateEnabled(): Flow<Boolean> = dataStore.data.map { it[CONNECT_ON_APP_UPDATE] ?: false }

    fun setEnabledShowGeoLocatedServers(enable: Boolean) {
        scope.launch {
            dataStore.edit { it[SHOW_GEO_SERVERS] = enable }
        }
    }

    fun isShowGeoLocatedServersEnabled(): Flow<Boolean> = dataStore.data.map { it[SHOW_GEO_SERVERS] ?: true }

    fun setSelectedProtocol(protocol: VpnProtocols) {
        scope.launch {
            dataStore.edit { it[SELECTED_PROTOCOL] = Json.encodeToString(protocol) }
        }
    }

    fun getSelectedProtocol(): Flow<VpnProtocols> =
        dataStore.data.map { prefs ->
            prefs[SELECTED_PROTOCOL]?.let { Json.decodeFromString(it) } ?: VpnProtocols.WireGuard
        }

    fun setWireGuardSettings(settings: WireGuardSettings) {
        scope.launch {
            dataStore.edit { it[WIRE_GUARD_SETTINGS] = Json.encodeToString(settings) }
        }
    }

    fun getWireGuardSettings(): Flow<WireGuardSettings> =
        dataStore.data.map { prefs ->
            prefs[WIRE_GUARD_SETTINGS]?.let { Json.decodeFromString(it) } ?: WireGuardSettings()
        }

    fun setOpenVpnSettings(settings: OpenVpnSettings) {
        scope.launch {
            dataStore.edit { it[OPEN_VPN_SETTINGS] = Json.encodeToString(settings) }
        }
    }

    fun getOpenVpnSettings(): Flow<OpenVpnSettings> =
        dataStore.data.map { prefs ->
            prefs[OPEN_VPN_SETTINGS]?.let { Json.decodeFromString(it) } ?: OpenVpnSettings()
        }

    fun setSelectedDnsOption(dnsOptions: DnsOptions) {
        scope.launch {
            dataStore.edit { it[SELECTED_DNS_OPTION_SETTINGS] = Json.encodeToString(dnsOptions) }
        }
    }

    fun getSelectedDnsOption(): Flow<DnsOptions> =
        dataStore.data.map { prefs ->
            prefs[SELECTED_DNS_OPTION_SETTINGS]?.let { Json.decodeFromString(it) } ?: DnsOptions.PIA
        }

    fun setCustomDns(customDns: CustomDns) {
        scope.launch {
            dataStore.edit { it[CUSTOM_DNS_SETTINGS] = Json.encodeToString(customDns) }
        }
    }

    fun getCustomDns(): Flow<CustomDns> =
        dataStore.data.map { prefs ->
            prefs[CUSTOM_DNS_SETTINGS]?.let { Json.decodeFromString(it) } ?: CustomDns()
        }

    fun setShadowsocksObfuscationEnabled(enabled: Boolean) {
        scope.launch {
            dataStore.edit { it[SHADOWSOCKS_OBFUSCATION_ENABLED] = enabled }
        }
    }

    fun isShadowsocksObfuscationEnabled(): Flow<Boolean> = dataStore.data.map { it[SHADOWSOCKS_OBFUSCATION_ENABLED] ?: false }

    fun setExternalProxyAppEnabled(enabled: Boolean) {
        scope.launch {
            dataStore.edit { it[EXTERNAL_PROXY_APP_ENABLED] = enabled }
        }
    }

    fun isExternalProxyAppEnabled(): Flow<Boolean> = dataStore.data.map { it[EXTERNAL_PROXY_APP_ENABLED] ?: false }

    fun setExternalProxyAppPackageName(packageName: String) {
        scope.launch {
            dataStore.edit { it[EXTERNAL_PROXY_APP_PACKAGE_NAME] = packageName }
        }
    }

    fun getExternalProxyAppPackageName(): Flow<String> = dataStore.data.map { it[EXTERNAL_PROXY_APP_PACKAGE_NAME] ?: "" }

    fun setSelectedObfuscationOption(obfuscationOptions: ObfuscationOptions) {
        scope.launch {
            dataStore.edit { it[SELECTED_OBFUSCATION_OPTION_SETTINGS] = Json.encodeToString(obfuscationOptions) }
        }
    }

    fun getSelectedObfuscationOption(): Flow<ObfuscationOptions> =
        dataStore.data.map { prefs ->
            prefs[SELECTED_OBFUSCATION_OPTION_SETTINGS]?.let { Json.decodeFromString(it) } ?: ObfuscationOptions.PIA
        }

    fun setCustomObfuscation(customObfuscation: CustomObfuscation) {
        scope.launch {
            dataStore.edit { it[CUSTOM_OBFUSCATION_SETTINGS] = Json.encodeToString(customObfuscation) }
        }
    }

    fun getCustomObfuscation(): Flow<CustomObfuscation?> =
        dataStore.data.map { prefs ->
            prefs[CUSTOM_OBFUSCATION_SETTINGS]?.let { Json.decodeFromString(it) }
        }

    fun setHelpImprovePiaEnabled(enable: Boolean) {
        scope.launch {
            dataStore.edit { it[HELP_IMPROVE_PIA] = enable }
        }
    }

    fun isHelpImprovePiaEnabled(): Flow<Boolean> = dataStore.data.map { it[HELP_IMPROVE_PIA] ?: false }

    fun setDebugLoggingEnabled(enable: Boolean) {
        scope.launch {
            dataStore.edit { it[DEBUG_LOGGING] = enable }
        }
    }

    fun isDebugLoggingEnabled(): Flow<Boolean> = dataStore.data.map { it[DEBUG_LOGGING] ?: false }

    fun setVpnExcludedApps(apps: List<String>) {
        scope.launch {
            dataStore.edit { it[VPN_EXCLUDED_APPS] = apps.toSet() }
        }
    }

    fun getVpnExcludedApps(): Flow<List<String>> = dataStore.data.map { it[VPN_EXCLUDED_APPS]?.toList() ?: emptyList() }

    fun setEnablePortForwarding(enable: Boolean) {
        scope.launch {
            dataStore.edit { it[PORT_FORWARDING] = enable }
        }
    }

    fun isPortForwardingEnabled(): Flow<Boolean> = dataStore.data.map { it[PORT_FORWARDING] ?: false }

    fun setAllowLocalTrafficEnabled(enable: Boolean) {
        scope.launch {
            dataStore.edit { it[ALLOW_LOCAL_TRAFFIC] = enable }
        }
    }

    fun isAllowLocalTrafficEnabled(): Flow<Boolean> = dataStore.data.map { it[ALLOW_LOCAL_TRAFFIC] ?: false }

    fun setAutomationEnabled(enable: Boolean) {
        scope.launch {
            dataStore.edit { it[AUTOMATION] = enable }
        }
    }

    fun isAutomationEnabled(): Flow<Boolean> = dataStore.data.map { it[AUTOMATION] ?: false }

    fun setMaceEnabled(enable: Boolean) {
        scope.launch {
            dataStore.edit { it[MACE] = enable }
        }
    }

    fun isMaceEnabled(): Flow<Boolean> = dataStore.data.map { it[MACE] ?: false }
}