package com.kape.localprefs.prefs

import android.content.Context
import com.kape.localprefs.Prefs
import org.koin.core.annotation.Singleton

private const val ACTIVE_PROTOCOL = "active-protocol"

@Singleton
class KpiPrefs(
    context: Context,
) : Prefs(context, "kpi") {
    fun setActiveProtocol(protocol: String) {
        prefs.edit().putString(ACTIVE_PROTOCOL, protocol).apply()
    }

    fun getActiveProtocol(): String = prefs.getString(ACTIVE_PROTOCOL, "")!!
}