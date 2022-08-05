package com.kape.share_events.utils

import android.content.Context
import com.kape.core.Prefs

private const val ACTIVE_PROTOCOL = "active-protocol"

class KpiPrefs(context: Context) : Prefs(context, "kpi") {

    fun setActiveProtocol(protocol: String) {
        prefs.edit().putString(ACTIVE_PROTOCOL, protocol).apply()
    }

    fun getActiveProtocol(): String {
        return prefs.getString(ACTIVE_PROTOCOL, "")!!
    }
}