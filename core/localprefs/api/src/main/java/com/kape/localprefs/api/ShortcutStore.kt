package com.kape.localprefs.api

import kotlinx.coroutines.flow.StateFlow

interface ShortcutStore {
    val isShortcutChangeServer: StateFlow<Boolean>

    val isShortcutConnectToVpn: StateFlow<Boolean>

    val isShortcutDisconnectVpn: StateFlow<Boolean>

    val isShortcutSettings: StateFlow<Boolean>

    suspend fun setShortcutChangeServer(isShortcut: Boolean)

    suspend fun setShortcutConnectToVpn(isShortcut: Boolean)

    suspend fun setShortcutDisconnectVpn(isShortcut: Boolean)

    suspend fun setShortcutSettings(isShortcut: Boolean)
}