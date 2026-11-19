package com.kape.vpn.shortcuts

fun interface AppShortcuts {
    fun publish(isConnected: Boolean)
}

fun interface ShortcutRequestRepository {
    suspend fun record(action: ShortcutAction)
}