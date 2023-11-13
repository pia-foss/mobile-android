package com.kape.csi

import android.content.Context
import com.kape.utils.Prefs

private const val LAST_KNOWN_EXCEPTION = "last-known-exception"

class CsiPrefs(context: Context) : Prefs(context, "csi") {

    fun setLastKnownException(value: String) =
        prefs.edit().putString(LAST_KNOWN_EXCEPTION, value).apply()

    fun getLastKnownException() = prefs.getString(LAST_KNOWN_EXCEPTION, "") ?: ""
}