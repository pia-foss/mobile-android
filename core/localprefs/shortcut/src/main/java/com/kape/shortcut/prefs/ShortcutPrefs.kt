package com.kape.shortcut.prefs

import android.content.Context
import com.kape.utils.Prefs

private const val SHORTCUT_CONNECT_TO_VPN = "shortcut-connect-to-vpn"
private const val SHORTCUT_CHANGE_SERVER = "shortcut-change-server"
private const val SHORTCUT_SETTINGS = "shortcut-settings"

class ShortcutPrefs(
    context: Context,
) : Prefs(context, "shortcut") {
    fun setShortcutConnectToVpn(isShortcut: Boolean) =
        prefs
            .edit()
            .putBoolean(
                SHORTCUT_CONNECT_TO_VPN,
                isShortcut,
            ).apply()

    fun isShortcutConnectToVpn(): Boolean = prefs.getBoolean(SHORTCUT_CONNECT_TO_VPN, false)

    fun setShortcutChangeServer(isShortcut: Boolean) =
        prefs
            .edit()
            .putBoolean(
                SHORTCUT_CHANGE_SERVER,
                isShortcut,
            ).apply()

    fun isShortcutChangeServer(): Boolean = prefs.getBoolean(SHORTCUT_CHANGE_SERVER, false)

    fun setShortcutSettings(isShortcut: Boolean) =
        prefs
            .edit()
            .putBoolean(
                SHORTCUT_SETTINGS,
                isShortcut,
            ).apply()

    fun isShortcutSettings(): Boolean = prefs.getBoolean(SHORTCUT_SETTINGS, false)
}