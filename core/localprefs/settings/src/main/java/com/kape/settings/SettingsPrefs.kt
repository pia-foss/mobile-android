package com.kape.settings

import android.content.Context
import com.kape.settings.data.CustomDns
import com.kape.settings.data.CustomObfuscation
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import com.kape.utils.Prefs
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val LAUNCH_ON_STARTUP = "launch-on-startup"
private const val CONNECT_ON_LAUNCH = "connect-on-launch"
private const val CONNECT_ON_APP_UPDATE = "connect-on-app-update"
private const val SHOW_GEO_SERVERS = "show-geo-located-servers"
private const val SELECTED_PROTOCOL = "selected-protocol"
private const val WIRE_GUARD_SETTINGS = "wireguard-settings"
private const val OPEN_VPN_SETTINGS = "openvpn-settings"
private const val SELECTED_DNS_OPTION_SETTINGS = "selected-dns-option-settings"
private const val CUSTOM_DNS_SETTINGS = "custom-dns-settings"
private const val SELECTED_OBFUSCATION_OPTION_SETTINGS = "selected-obfuscation-option-settings"
private const val CUSTOM_OBFUSCATION_SETTINGS = "custom-obfuscation-settings"
private const val SHADOWSOCKS_OBFUSCATION_ENABLED = "shadowsocks-obfuscation-enabled"
private const val EXTERNAL_PROXY_APP_ENABLED = "external-proxy-app-enabled"
private const val EXTERNAL_PROXY_APP_PACKAGE_NAME = "external-proxy-app-package-name"
private const val HELP_IMPROVE_PIA = "help-improve-pia"
private const val DEBUG_LOGGING = "debug-logging"
private const val VPN_EXCLUDED_APPS = "vpn-excluded-apps"
private const val PORT_FORWARDING = "port-forwarding"
private const val ALLOW_LOCAL_TRAFFIC = "allow-local-traffic"
private const val AUTOMATION = "setting-automation"
private const val MACE = "setting-mace"

class SettingsPrefs(
    context: Context,
) : Prefs(context, "settings") {
    fun setEnableLaunchOnStartup(enable: Boolean) {
        prefs.edit().putBoolean(LAUNCH_ON_STARTUP, enable).apply()
    }

    fun isLaunchOnStartupEnabled(): Boolean = prefs.getBoolean(LAUNCH_ON_STARTUP, false)

    fun setEnableConnectOnLaunch(enable: Boolean) {
        prefs.edit().putBoolean(CONNECT_ON_LAUNCH, enable).apply()
    }

    fun isConnectOnLaunchEnabled(): Boolean = prefs.getBoolean(CONNECT_ON_LAUNCH, false)

    fun setEnableConnectOnAppUpdate(enable: Boolean) {
        prefs.edit().putBoolean(CONNECT_ON_APP_UPDATE, enable).apply()
    }

    fun isConnectOnAppUpdateEnabled(): Boolean = prefs.getBoolean(CONNECT_ON_APP_UPDATE, false)

    fun setEnabledShowGeoLocatedServers(enable: Boolean) {
        prefs.edit().putBoolean(SHOW_GEO_SERVERS, enable).apply()
    }

    fun isShowGeoLocatedServersEnabled(): Boolean = prefs.getBoolean(SHOW_GEO_SERVERS, true)

    fun setSelectedProtocol(protocol: VpnProtocols) {
        prefs.edit().putString(SELECTED_PROTOCOL, Json.encodeToString(protocol)).apply()
    }

    fun getSelectedProtocol(): VpnProtocols {
        prefs
            .getString(
                SELECTED_PROTOCOL,
                Json.encodeToString(VpnProtocols.WireGuard),
            )?.let {
                return Json.decodeFromString(it)
            } ?: run {
            return VpnProtocols.WireGuard
        }
    }

    fun setWireGuardSettings(settings: WireGuardSettings) {
        prefs.edit().putString(WIRE_GUARD_SETTINGS, Json.encodeToString(settings)).apply()
    }

    fun getWireGuardSettings(): WireGuardSettings {
        prefs.getString(WIRE_GUARD_SETTINGS, null)?.let {
            return Json.decodeFromString(it)
        } ?: run {
            return WireGuardSettings()
        }
    }

    fun setOpenVpnSettings(settings: OpenVpnSettings) {
        prefs.edit().putString(OPEN_VPN_SETTINGS, Json.encodeToString(settings)).apply()
    }

    fun getOpenVpnSettings(): OpenVpnSettings {
        prefs.getString(OPEN_VPN_SETTINGS, null)?.let {
            return Json.decodeFromString(it)
        } ?: run {
            return OpenVpnSettings()
        }
    }

    fun setSelectedDnsOption(dnsOptions: DnsOptions) {
        prefs
            .edit()
            .putString(SELECTED_DNS_OPTION_SETTINGS, Json.encodeToString(dnsOptions))
            .apply()
    }

    fun getSelectedDnsOption(): DnsOptions {
        prefs.getString(SELECTED_DNS_OPTION_SETTINGS, null)?.let {
            return Json.decodeFromString(it)
        } ?: run {
            return DnsOptions.PIA
        }
    }

    fun setCustomDns(customDns: CustomDns) {
        prefs.edit().putString(CUSTOM_DNS_SETTINGS, Json.encodeToString(customDns)).apply()
    }

    fun getCustomDns(): CustomDns {
        prefs.getString(CUSTOM_DNS_SETTINGS, null)?.let {
            return Json.decodeFromString(it)
        } ?: run {
            return CustomDns()
        }
    }

    fun setShadowsocksObfuscationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(SHADOWSOCKS_OBFUSCATION_ENABLED, enabled).apply()
    }

    fun isShadowsocksObfuscationEnabled(): Boolean = prefs.getBoolean(SHADOWSOCKS_OBFUSCATION_ENABLED, false)

    fun setExternalProxyAppEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(EXTERNAL_PROXY_APP_ENABLED, enabled).apply()
    }

    fun isExternalProxyAppEnabled() = prefs.getBoolean(EXTERNAL_PROXY_APP_ENABLED, false)

    fun setExternalProxyAppPackageName(packageName: String) =
        prefs
            .edit()
            .putString(
                EXTERNAL_PROXY_APP_PACKAGE_NAME,
                packageName,
            ).apply()

    fun getExternalProxyAppPackageName(): String = prefs.getString(EXTERNAL_PROXY_APP_PACKAGE_NAME, "") ?: ""

    fun setSelectedObfuscationOption(obfuscationOptions: ObfuscationOptions) {
        prefs
            .edit()
            .putString(
                SELECTED_OBFUSCATION_OPTION_SETTINGS,
                Json.encodeToString(obfuscationOptions),
            ).apply()
    }

    fun getSelectedObfuscationOption(): ObfuscationOptions {
        prefs.getString(SELECTED_OBFUSCATION_OPTION_SETTINGS, null)?.let {
            return Json.decodeFromString(it)
        } ?: run {
            return ObfuscationOptions.PIA
        }
    }

    fun setCustomObfuscation(customObfuscation: CustomObfuscation) {
        prefs
            .edit()
            .putString(CUSTOM_OBFUSCATION_SETTINGS, Json.encodeToString(customObfuscation))
            .apply()
    }

    fun getCustomObfuscation(): CustomObfuscation? {
        prefs.getString(CUSTOM_OBFUSCATION_SETTINGS, null)?.let {
            return Json.decodeFromString(it)
        } ?: run {
            return null
        }
    }

    fun setHelpImprovePiaEnabled(enable: Boolean) = prefs.edit().putBoolean(HELP_IMPROVE_PIA, enable).apply()

    fun isHelpImprovePiaEnabled() = prefs.getBoolean(HELP_IMPROVE_PIA, false)

    fun setDebugLoggingEnabled(enable: Boolean) = prefs.edit().putBoolean(DEBUG_LOGGING, enable).apply()

    fun isDebugLoggingEnabled() = prefs.getBoolean(DEBUG_LOGGING, false)

    fun setVpnExcludedApps(apps: List<String>) = prefs.edit().putStringSet(VPN_EXCLUDED_APPS, apps.toSet()).apply()

    fun getVpnExcludedApps() = prefs.getStringSet(VPN_EXCLUDED_APPS, emptySet())?.toList() ?: emptyList()

    fun setEnablePortForwarding(enable: Boolean) = prefs.edit().putBoolean(PORT_FORWARDING, enable).apply()

    fun isPortForwardingEnabled() = prefs.getBoolean(PORT_FORWARDING, false)

    fun setAllowLocalTrafficEnabled(enable: Boolean) = prefs.edit().putBoolean(ALLOW_LOCAL_TRAFFIC, enable).apply()

    fun isAllowLocalTrafficEnabled() = prefs.getBoolean(ALLOW_LOCAL_TRAFFIC, false)

    fun setAutomationEnabled(enable: Boolean) = prefs.edit().putBoolean(AUTOMATION, enable).apply()

    fun isAutomationEnabled() = prefs.getBoolean(AUTOMATION, false)

    fun setMaceEnabled(enable: Boolean) = prefs.edit().putBoolean(MACE, enable).apply()

    fun isMaceEnabled() = prefs.getBoolean(MACE, false)
}