package com.kape.localprefs.prefs

import android.content.Context
import com.kape.localprefs.Prefs
import org.koin.core.annotation.Singleton

private const val SHORTCUT_CONNECT_TO_VPN = "shortcut-connect-to-vpn"
private const val SHORTCUT_DISCONNECT_VPN = "shortcut-disconnect-vpn"
private const val SHORTCUT_CHANGE_SERVER = "shortcut-change-server"
private const val SHORTCUT_SETTINGS = "shortcut-settings"

@Singleton
class ShortcutPrefs(
    context: Context,
) : Prefs(context, "shortcut") {
    fun setShortcutConnectToVpn(isShortcut: Boolean) {
        prefs
            .edit()
            .putBoolean(
                SHORTCUT_CONNECT_TO_VPN,
                isShortcut,
            ).apply()
        if (isShortcut) {
            setShortcutDisconnectVpn(false)
            setShortcutSettings(false)
            setShortcutChangeServer(false)
        }
    }

    fun isShortcutConnectToVpn(): Boolean = prefs.getBoolean(SHORTCUT_CONNECT_TO_VPN, false)

    fun setShortcutDisconnectVpn(isShortcut: Boolean) {
        prefs
            .edit()
            .putBoolean(
                SHORTCUT_DISCONNECT_VPN,
                isShortcut,
            ).apply()
        if (isShortcut) {
            setShortcutSettings(false)
            setShortcutChangeServer(false)
            setShortcutConnectToVpn(false)
        }
    }

    fun isShortcutDisconnectVpn(): Boolean = prefs.getBoolean(SHORTCUT_DISCONNECT_VPN, false)

    fun setShortcutChangeServer(isShortcut: Boolean) {
        prefs
            .edit()
            .putBoolean(
                SHORTCUT_CHANGE_SERVER,
                isShortcut,
            ).apply()
        if (isShortcut) {
            setShortcutSettings(false)
            setShortcutConnectToVpn(false)
            setShortcutDisconnectVpn(false)
        }
    }

    fun isShortcutChangeServer(): Boolean = prefs.getBoolean(SHORTCUT_CHANGE_SERVER, false)

    fun setShortcutSettings(isShortcut: Boolean) {
        prefs
            .edit()
            .putBoolean(
                SHORTCUT_SETTINGS,
                isShortcut,
            ).apply()
        if (isShortcut) {
            setShortcutChangeServer(false)
            setShortcutConnectToVpn(false)
            setShortcutDisconnectVpn(false)
        }
    }

    fun isShortcutSettings(): Boolean = prefs.getBoolean(SHORTCUT_SETTINGS, false)
}