package com.kape.csi

import android.content.Context
import com.kape.utils.Prefs

private const val LAST_KNOWN_EXCEPTION = "last-known-exception"
private const val PROTOCOL_DEBUG_LOGS = "protocol-debug-logs"

class CsiPrefs(context: Context) : Prefs(context, "csi") {

    fun setLastKnownException(value: String) =
        prefs.edit().putString(LAST_KNOWN_EXCEPTION, value).apply()

    fun getLastKnownException() = prefs.getString(LAST_KNOWN_EXCEPTION, "") ?: ""

    fun setProtocolDebugLogs(value: String) =
        prefs.edit().putString(PROTOCOL_DEBUG_LOGS, value).apply()

    fun getProtocolDebugLogs() = prefs.getString(PROTOCOL_DEBUG_LOGS, "") ?: ""
}