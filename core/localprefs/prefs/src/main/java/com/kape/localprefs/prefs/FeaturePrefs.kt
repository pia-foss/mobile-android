package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kape.localprefs.Prefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

private val FEATURE_FLAGS = stringPreferencesKey("feature-flags")
const val SUPPORT_DIALOG_FEATURE_FLAG = "login-support-dialog"

@Singleton
class FeaturePrefs(
    context: Context,
) : Prefs(context, "feature-flags") {
    suspend fun setFlags(flags: List<String>) {
        dataStore.edit { it[FEATURE_FLAGS] = Json.encodeToString(flags) }
    }

    fun getFlags(): Flow<List<String>> =
        dataStore.data.map { prefs ->
            prefs[FEATURE_FLAGS]?.let { Json.decodeFromString(it) } ?: emptyList()
        }
}