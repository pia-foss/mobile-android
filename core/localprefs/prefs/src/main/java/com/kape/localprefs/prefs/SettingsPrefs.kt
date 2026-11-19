package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.kape.contracts.AppInfo
import com.kape.contracts.di.Qualifiers
import com.kape.localprefs.Prefs
import com.kape.localprefs.api.SettingsStore
import com.kape.localprefs.data.settings.AutomaticSettings
import com.kape.settings.data.CustomDns
import com.kape.settings.data.CustomObfuscation
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

private val LAUNCH_ON_STARTUP = booleanPreferencesKey("launch-on-startup")
private val CONNECT_ON_LAUNCH = booleanPreferencesKey("connect-on-launch")
private val CONNECT_ON_APP_UPDATE = booleanPreferencesKey("connect-on-app-update")
private val SHOW_GEO_SERVERS = booleanPreferencesKey("show-geo-located-servers")
private val SELECTED_PROTOCOL = stringPreferencesKey("selected-protocol")
private val PROTOCOL_DEFAULT_PINNED = booleanPreferencesKey("protocol-default-pinned")
private val WIRE_GUARD_SETTINGS = stringPreferencesKey("wireguard-settings")
private val OPEN_VPN_SETTINGS = stringPreferencesKey("openvpn-settings")
private val AUTO_SETTINGS = stringPreferencesKey("auto-settings")
private val SELECTED_DNS_OPTION_SETTINGS = stringPreferencesKey("selected-dns-option-settings")
private val CUSTOM_DNS_SETTINGS = stringPreferencesKey("custom-dns-settings")
private val SELECTED_OBFUSCATION_OPTION_SETTINGS =
    stringPreferencesKey("selected-obfuscation-option-settings")
private val CUSTOM_OBFUSCATION_SETTINGS = stringPreferencesKey("custom-obfuscation-settings")
private val SHADOWSOCKS_OBFUSCATION_ENABLED =
    booleanPreferencesKey("shadowsocks-obfuscation-enabled")
private val EXTERNAL_PROXY_APP_ENABLED = booleanPreferencesKey("external-proxy-app-enabled")
private val EXTERNAL_PROXY_APP_PACKAGE_NAME =
    stringPreferencesKey("external-proxy-app-package-name")
private val DEBUG_LOGGING = booleanPreferencesKey("debug-logging")
private val VPN_EXCLUDED_APPS = stringSetPreferencesKey("vpn-excluded-apps")
private val PORT_FORWARDING = booleanPreferencesKey("port-forwarding")
private val ALLOW_LOCAL_TRAFFIC = booleanPreferencesKey("allow-local-traffic")
private val AUTOMATION = booleanPreferencesKey("setting-automation")
private val MACE = booleanPreferencesKey("setting-mace")
private val MOCK_LOCATION = booleanPreferencesKey("setting-mock-location")

// Previously stored JSON still carries a stale `mtu` key that must be ignored.
internal val protocolSettingsJson = Json { ignoreUnknownKeys = true }

internal fun resolveLegacyProtocolBackfill(
    isFreshInstall: Boolean,
    alreadyPinned: Boolean,
    hasExplicitSelection: Boolean,
): VpnProtocols? =
    if (!alreadyPinned && !isFreshInstall && !hasExplicitSelection) {
        VpnProtocols.WireGuard
    } else {
        null
    }

@Singleton(binds = [SettingsPrefs::class, SettingsStore::class])
class SettingsPrefs(
    context: Context,
    private val appInfo: AppInfo,
    @Named(Qualifiers.APP_SCOPE) appScope: CoroutineScope,
    @Named(Qualifiers.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
) : Prefs(context, "settings", appScope, ioDispatcher),
    SettingsStore {
    // Existing installs must keep resolving WireGuard even after the fallback below changes to
    // Automatic; only a device that has never been upgraded gets the new default. Every read of
    // the selected protocol waits on this so there's no race with the one-time backfill.
    private val protocolDefaultPinned = CompletableDeferred<Unit>()

    init {
        scope.launch {
            pinLegacyProtocolDefaultIfNeeded()
            protocolDefaultPinned.complete(Unit)
        }
    }

    override val isLaunchOnStartupEnabled: StateFlow<Boolean> =
        getLaunchOnStartupEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    override val isConnectOnLaunchEnabled: StateFlow<Boolean> =
        getConnectOnLaunchEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    override val isConnectOnAppUpdateEnabled: StateFlow<Boolean> =
        getConnectOnAppUpdateEnabled().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            false,
        )
    override val isShowGeoLocatedServersEnabled: StateFlow<Boolean> =
        getShowGeoLocatedServersEnabled().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            true,
        )
    override val selectedProtocol: StateFlow<VpnProtocols> =
        getSelectedProtocol().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            VpnProtocols.Automatic,
        )
    override val wireGuardSettings: StateFlow<WireGuardSettings> =
        getWireGuardSettings().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            WireGuardSettings(),
        )
    override val openVpnSettings: StateFlow<OpenVpnSettings> =
        getOpenVpnSettings().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            OpenVpnSettings(),
        )

    override val autoSettings: StateFlow<AutomaticSettings> =
        getAutoSettings().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            AutomaticSettings(),
        )

    override val selectedDnsOption: StateFlow<DnsOptions> =
        getSelectedDnsOption().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            DnsOptions.PIA,
        )
    override val customDns: StateFlow<CustomDns> =
        getCustomDns().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), CustomDns())
    override val isShadowsocksObfuscationEnabled: StateFlow<Boolean> =
        getShadowsocksObfuscationEnabled().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            false,
        )
    override val isExternalProxyAppEnabled: StateFlow<Boolean> =
        getExternalProxyAppEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    override val externalProxyAppPackageName: StateFlow<String> =
        getExternalProxyAppPackageName().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            "",
        )
    override val selectedObfuscationOption: StateFlow<ObfuscationOptions> =
        getSelectedObfuscationOption().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            ObfuscationOptions.PIA,
        )
    override val customObfuscation: StateFlow<CustomObfuscation?> =
        getCustomObfuscation().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), null)
    override val isDebugLoggingEnabled: StateFlow<Boolean> =
        getDebugLoggingEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    override val vpnExcludedApps: StateFlow<List<String>> =
        getVpnExcludedApps().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), emptyList())
    override val isPortForwardingEnabled: StateFlow<Boolean> =
        getPortForwardingEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    override val isAllowLocalTrafficEnabled: StateFlow<Boolean> =
        getAllowLocalTrafficEnabled().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            false,
        )
    override val isAutomationEnabled: StateFlow<Boolean> =
        getAutomationEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    override val isMaceEnabled: StateFlow<Boolean> =
        getMaceEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    override val isMockLocationEnabled: StateFlow<Boolean> =
        getMockLocationEnabled().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)

    override suspend fun setEnableLaunchOnStartup(enabled: Boolean) {
        dataStore.edit { it[LAUNCH_ON_STARTUP] = enabled }
    }

    override suspend fun setEnableConnectOnLaunch(enabled: Boolean) {
        dataStore.edit { it[CONNECT_ON_LAUNCH] = enabled }
    }

    override suspend fun setEnableConnectOnAppUpdate(enabled: Boolean) {
        dataStore.edit { it[CONNECT_ON_APP_UPDATE] = enabled }
    }

    override suspend fun setEnabledShowGeoLocatedServers(enabled: Boolean) {
        dataStore.edit { it[SHOW_GEO_SERVERS] = enabled }
    }

    override suspend fun setSelectedProtocol(protocol: VpnProtocols) {
        dataStore.edit { it[SELECTED_PROTOCOL] = Json.encodeToString(protocol) }
    }

    override suspend fun setWireGuardSettings(settings: WireGuardSettings) {
        dataStore.edit { it[WIRE_GUARD_SETTINGS] = protocolSettingsJson.encodeToString(settings) }
    }

    override suspend fun setOpenVpnSettings(settings: OpenVpnSettings) {
        dataStore.edit { it[OPEN_VPN_SETTINGS] = protocolSettingsJson.encodeToString(settings) }
    }

    override suspend fun setAutoSettings(settings: AutomaticSettings) {
        dataStore.edit { it[AUTO_SETTINGS] = protocolSettingsJson.encodeToString(settings) }
    }

    override suspend fun setSelectedDnsOption(dnsOptions: DnsOptions) {
        dataStore.edit { it[SELECTED_DNS_OPTION_SETTINGS] = Json.encodeToString(dnsOptions) }
    }

    override suspend fun setCustomDns(customDns: CustomDns) {
        dataStore.edit { it[CUSTOM_DNS_SETTINGS] = Json.encodeToString(customDns) }
    }

    override suspend fun getSelectedProtocolNow(): VpnProtocols = getSelectedProtocol().first()

    override suspend fun getOpenVpnSettingsNow(): OpenVpnSettings = getOpenVpnSettings().first()

    override suspend fun getWireGuardSettingsNow(): WireGuardSettings = getWireGuardSettings().first()

    override suspend fun getAutoSettingsNow(): AutomaticSettings = getAutoSettings().first()

    suspend fun isMaceEnabledNow(): Boolean = getMaceEnabled().first()

    suspend fun getSelectedDnsOptionNow(): DnsOptions = getSelectedDnsOption().first()

    suspend fun getCustomDnsNow(): CustomDns = getCustomDns().first()

    override suspend fun getVpnExcludedAppsNow(): List<String> = getVpnExcludedApps().first()

    suspend fun isAllowLocalTrafficEnabledNow(): Boolean = getAllowLocalTrafficEnabled().first()

    override suspend fun isAutomationEnabledNow(): Boolean = getAutomationEnabled().first()

    suspend fun isExternalProxyAppEnabledNow(): Boolean = getExternalProxyAppEnabled().first()

    override suspend fun getExternalProxyAppPackageNameNow(): String = getExternalProxyAppPackageName().first()

    suspend fun isShadowsocksObfuscationEnabledNow(): Boolean = getShadowsocksObfuscationEnabled().first()

    suspend fun isConnectOnAppUpdateEnabledNow(): Boolean = getConnectOnAppUpdateEnabled().first()

    suspend fun isPortForwardingEnabledNow(): Boolean = getPortForwardingEnabled().first()

    suspend fun isLaunchOnBootEnabledNow(): Boolean = getLaunchOnStartupEnabled().first()

    override suspend fun isConnectOnLaunchEnabledNow(): Boolean = getConnectOnLaunchEnabled().first()

    override suspend fun setShadowsocksObfuscationEnabled(enabled: Boolean) {
        dataStore.edit { it[SHADOWSOCKS_OBFUSCATION_ENABLED] = enabled }
    }

    override suspend fun setExternalProxyAppEnabled(enabled: Boolean) {
        dataStore.edit { it[EXTERNAL_PROXY_APP_ENABLED] = enabled }
    }

    override suspend fun setExternalProxyAppPackageName(packageName: String) {
        dataStore.edit { it[EXTERNAL_PROXY_APP_PACKAGE_NAME] = packageName }
    }

    override suspend fun setSelectedObfuscationOption(obfuscationOptions: ObfuscationOptions) {
        dataStore.edit {
            it[SELECTED_OBFUSCATION_OPTION_SETTINGS] = Json.encodeToString(obfuscationOptions)
        }
    }

    override suspend fun setCustomObfuscation(customObfuscation: CustomObfuscation) {
        dataStore.edit { it[CUSTOM_OBFUSCATION_SETTINGS] = Json.encodeToString(customObfuscation) }
    }

    override suspend fun setDebugLoggingEnabled(enabled: Boolean) {
        dataStore.edit { it[DEBUG_LOGGING] = enabled }
    }

    override suspend fun setVpnExcludedApps(apps: List<String>) {
        dataStore.edit { it[VPN_EXCLUDED_APPS] = apps.toSet() }
    }

    override suspend fun setEnablePortForwarding(enabled: Boolean) {
        dataStore.edit { it[PORT_FORWARDING] = enabled }
    }

    override suspend fun setAllowLocalTrafficEnabled(enabled: Boolean) {
        dataStore.edit { it[ALLOW_LOCAL_TRAFFIC] = enabled }
    }

    override suspend fun setAutomationEnabled(enabled: Boolean) {
        dataStore.edit { it[AUTOMATION] = enabled }
    }

    override suspend fun setMaceEnabled(enabled: Boolean) {
        dataStore.edit { it[MACE] = enabled }
    }

    override suspend fun setMockLocationEnabled(enabled: Boolean) {
        dataStore.edit { it[MOCK_LOCATION] = enabled }
    }

    private fun getLaunchOnStartupEnabled(): Flow<Boolean> = dataStore.data.map { it[LAUNCH_ON_STARTUP] ?: false }

    private fun getConnectOnLaunchEnabled(): Flow<Boolean> = dataStore.data.map { it[CONNECT_ON_LAUNCH] ?: false }

    private fun getConnectOnAppUpdateEnabled(): Flow<Boolean> = dataStore.data.map { it[CONNECT_ON_APP_UPDATE] ?: false }

    private fun getShowGeoLocatedServersEnabled(): Flow<Boolean> = dataStore.data.map { it[SHOW_GEO_SERVERS] ?: true }

    private suspend fun pinLegacyProtocolDefaultIfNeeded() {
        dataStore.edit { prefs ->
            val protocolToBackfill =
                resolveLegacyProtocolBackfill(
                    isFreshInstall = appInfo.isFreshInstall,
                    alreadyPinned = prefs[PROTOCOL_DEFAULT_PINNED] == true,
                    hasExplicitSelection = prefs[SELECTED_PROTOCOL] != null,
                )
            protocolToBackfill?.let { prefs[SELECTED_PROTOCOL] = Json.encodeToString(it) }
            prefs[PROTOCOL_DEFAULT_PINNED] = true
        }
    }

    private fun getSelectedProtocol(): Flow<VpnProtocols> =
        dataStore.data
            .onStart { protocolDefaultPinned.await() }
            .map { prefs ->
                prefs[SELECTED_PROTOCOL]?.let { Json.decodeFromString(it) } ?: VpnProtocols.Automatic
            }

    private fun getWireGuardSettings(): Flow<WireGuardSettings> =
        dataStore.data.map { prefs ->
            prefs[WIRE_GUARD_SETTINGS]?.let { protocolSettingsJson.decodeFromString(it) } ?: WireGuardSettings()
        }

    private fun getOpenVpnSettings(): Flow<OpenVpnSettings> =
        dataStore.data.map { prefs ->
            prefs[OPEN_VPN_SETTINGS]?.let { protocolSettingsJson.decodeFromString(it) } ?: OpenVpnSettings()
        }

    private fun getAutoSettings(): Flow<AutomaticSettings> =
        dataStore.data.map { prefs ->
            prefs[AUTO_SETTINGS]?.let { protocolSettingsJson.decodeFromString(it) } ?: AutomaticSettings()
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
            prefs[SELECTED_OBFUSCATION_OPTION_SETTINGS]?.let { Json.decodeFromString(it) }
                ?: ObfuscationOptions.PIA
        }

    private fun getCustomObfuscation(): Flow<CustomObfuscation?> =
        dataStore.data.map { prefs ->
            prefs[CUSTOM_OBFUSCATION_SETTINGS]?.let { Json.decodeFromString(it) }
        }

    private fun getDebugLoggingEnabled(): Flow<Boolean> = dataStore.data.map { it[DEBUG_LOGGING] ?: false }

    private fun getVpnExcludedApps(): Flow<List<String>> = dataStore.data.map { it[VPN_EXCLUDED_APPS]?.toList() ?: emptyList() }

    private fun getPortForwardingEnabled(): Flow<Boolean> = dataStore.data.map { it[PORT_FORWARDING] ?: false }

    private fun getAllowLocalTrafficEnabled(): Flow<Boolean> = dataStore.data.map { it[ALLOW_LOCAL_TRAFFIC] ?: false }

    private fun getAutomationEnabled(): Flow<Boolean> = dataStore.data.map { it[AUTOMATION] ?: false }

    private fun getMaceEnabled(): Flow<Boolean> = dataStore.data.map { it[MACE] ?: false }

    private fun getMockLocationEnabled(): Flow<Boolean> = dataStore.data.map { it[MOCK_LOCATION] ?: false }
}