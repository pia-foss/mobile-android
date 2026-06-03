package com.kape.localprefs

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

internal class EncryptedSharedPrefsMigration(
    private val context: Context,
    private val name: String,
) : DataMigration<Preferences> {
    private val legacyPrefs by lazy {
        try {
            val masterKey =
                MasterKey
                    .Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
            EncryptedSharedPreferences(
                context,
                name,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun shouldMigrate(currentData: Preferences): Boolean = legacyPrefs?.all?.isNotEmpty() == true

    override suspend fun migrate(currentData: Preferences): Preferences {
        val mutable = currentData.toMutablePreferences()
        legacyPrefs?.all?.forEach { (key, value) -> mutable.putEntry(key, value) }
        return mutable.toPreferences()
    }

    override suspend fun cleanUp() {
        legacyPrefs?.edit()?.clear()?.apply()
    }

    @Suppress("UNCHECKED_CAST")
    private fun MutablePreferences.putEntry(
        key: String,
        value: Any?,
    ) {
        when (value) {
            is Boolean -> set(booleanPreferencesKey(key), value)
            is Int -> set(intPreferencesKey(key), value)
            is Long -> set(longPreferencesKey(key), value)
            is Float -> set(floatPreferencesKey(key), value)
            is String -> set(stringPreferencesKey(key), value)
            is Set<*> -> set(stringSetPreferencesKey(key), value as Set<String>)
        }
    }
}