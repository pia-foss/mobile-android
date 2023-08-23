package com.kape.settings

import android.content.Context
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import com.kape.utils.Prefs
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val LAUNCH_ON_STARTUP = "launch-on-startup"
private const val CONNECT_ON_LAUNCH = "connect-on-launch"
private const val CONNECT_ON_APP_UPDATE = "connect-on-app-update"
private const val SELECTED_PROTOCOL = "selected-protocol"
private const val WIRE_GUARD_SETTINGS = "wireguard-settings"
private const val OPEN_VPN_SETTINGS = "openvpn-settings"

class SettingsPrefs(context: Context) : Prefs(context, "settings") {

    fun setEnableLaunchOnStartup(enable: Boolean) {
        prefs.edit().putBoolean(LAUNCH_ON_STARTUP, enable).apply()
    }

    fun isLaunchOnStartupEnabled(): Boolean {
        return prefs.getBoolean(LAUNCH_ON_STARTUP, false)
    }

    fun setEnableConnectOnLaunch(enable: Boolean) {
        prefs.edit().putBoolean(CONNECT_ON_LAUNCH, enable).apply()
    }

    fun isConnectOnLaunchEnabled(): Boolean {
        return prefs.getBoolean(CONNECT_ON_LAUNCH, false)
    }

    fun setEnableConnectOnAppUpdate(enable: Boolean) {
        prefs.edit().putBoolean(CONNECT_ON_APP_UPDATE, enable).apply()
    }

    fun isConnectOnAppUpdateEnabled(): Boolean {
        return prefs.getBoolean(CONNECT_ON_APP_UPDATE, false)
    }

    fun setSelectedProtocol(protocol: VpnProtocols) {
        prefs.edit().putString(SELECTED_PROTOCOL, Json.encodeToString(protocol)).apply()
    }

    fun getSelectedProtocol(): VpnProtocols {
        prefs.getString(
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
}