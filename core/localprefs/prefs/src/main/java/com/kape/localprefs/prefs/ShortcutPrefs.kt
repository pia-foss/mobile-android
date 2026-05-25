package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.kape.localprefs.Prefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton

private val SHORTCUT_CONNECT_TO_VPN = booleanPreferencesKey("shortcut-connect-to-vpn")
private val SHORTCUT_DISCONNECT_VPN = booleanPreferencesKey("shortcut-disconnect-vpn")
private val SHORTCUT_CHANGE_SERVER = booleanPreferencesKey("shortcut-change-server")
private val SHORTCUT_SETTINGS = booleanPreferencesKey("shortcut-settings")

@Singleton
class ShortcutPrefs(
    context: Context,
) : Prefs(context, "shortcut") {
    fun setShortcutConnectToVpn(isShortcut: Boolean) {
        scope.launch {
            dataStore.edit { it[SHORTCUT_CONNECT_TO_VPN] = isShortcut }
        }
        if (isShortcut) {
            setShortcutDisconnectVpn(false)
            setShortcutSettings(false)
            setShortcutChangeServer(false)
        }
    }

    fun isShortcutConnectToVpn(): Flow<Boolean> = dataStore.data.map { it[SHORTCUT_CONNECT_TO_VPN] ?: false }

    fun setShortcutDisconnectVpn(isShortcut: Boolean) {
        scope.launch {
            dataStore.edit { it[SHORTCUT_DISCONNECT_VPN] = isShortcut }
        }
        if (isShortcut) {
            setShortcutSettings(false)
            setShortcutChangeServer(false)
            setShortcutConnectToVpn(false)
        }
    }

    fun isShortcutDisconnectVpn(): Flow<Boolean> = dataStore.data.map { it[SHORTCUT_DISCONNECT_VPN] ?: false }

    fun setShortcutChangeServer(isShortcut: Boolean) {
        scope.launch {
            dataStore.edit { it[SHORTCUT_CHANGE_SERVER] = isShortcut }
        }
        if (isShortcut) {
            setShortcutSettings(false)
            setShortcutConnectToVpn(false)
            setShortcutDisconnectVpn(false)
        }
    }

    fun isShortcutChangeServer(): Flow<Boolean> = dataStore.data.map { it[SHORTCUT_CHANGE_SERVER] ?: false }

    fun setShortcutSettings(isShortcut: Boolean) {
        scope.launch {
            dataStore.edit { it[SHORTCUT_SETTINGS] = isShortcut }
        }
        if (isShortcut) {
            setShortcutChangeServer(false)
            setShortcutConnectToVpn(false)
            setShortcutDisconnectVpn(false)
        }
    }

    fun isShortcutSettings(): Flow<Boolean> = dataStore.data.map { it[SHORTCUT_SETTINGS] ?: false }
}