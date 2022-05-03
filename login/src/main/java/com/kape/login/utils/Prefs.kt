package com.kape.login.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

private const val IS_USER_LOGGED_IN = "isUserLoggedIn"

class Prefs(context: Context, name: String) {

    private val prefs: SharedPreferences

    init {
        val key = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        prefs = EncryptedSharedPreferences.create(name,
            key,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }

    fun setUserLoggedIn(isLoggedIn: Boolean) =
        prefs.edit().putBoolean(IS_USER_LOGGED_IN, isLoggedIn).apply()

    fun isUserLoggedIn() = prefs.getBoolean(IS_USER_LOGGED_IN, false)
}