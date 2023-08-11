package com.kape.signup.utils

import android.content.Context
import com.kape.utils.Prefs

private const val SHARE_EVENTS_CONSENT = "share-events-consent"

class ConsentPrefs(context: Context) : Prefs(context, "consent") {

    fun setAllowSharing(allow: Boolean) =
        prefs.edit().putBoolean(SHARE_EVENTS_CONSENT, allow).apply()

    fun getAllowSharing() = prefs.getBoolean(SHARE_EVENTS_CONSENT, false)
}