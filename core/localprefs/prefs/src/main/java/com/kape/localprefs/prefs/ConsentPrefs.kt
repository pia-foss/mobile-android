package com.kape.localprefs.prefs

import android.content.Context
import com.kape.utils.Prefs
import org.koin.core.annotation.Singleton

private const val SHARE_EVENTS_CONSENT = "share-events-consent"

@Singleton
class ConsentPrefs(context: Context) : Prefs(context, "consent") {

    fun setAllowSharing(allow: Boolean) =
        prefs.edit().putBoolean(SHARE_EVENTS_CONSENT, allow).apply()

    fun getAllowSharing() = prefs.getBoolean(SHARE_EVENTS_CONSENT, false)
}