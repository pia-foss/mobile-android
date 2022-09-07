package com.kape.connection.utils

import android.content.Context
import com.kape.core.Prefs

private const val QUICK_CONNECT = "quick-connect-list"

class ConnectionPrefs(context: Context) : Prefs(context, "connection") {

    fun addToQuickConnect(serverKey: String) {
        val quickConnectList =
            prefs.getStringSet(QUICK_CONNECT, mutableSetOf())?.toMutableList() ?: mutableListOf()
        quickConnectList.add(serverKey)
        prefs.edit().putStringSet(QUICK_CONNECT, quickConnectList.toSet()).apply()
    }

    fun getQuickConnectServers(): List<String> {
        return prefs.getStringSet(QUICK_CONNECT, emptySet())?.toList() ?: emptyList()
    }
}