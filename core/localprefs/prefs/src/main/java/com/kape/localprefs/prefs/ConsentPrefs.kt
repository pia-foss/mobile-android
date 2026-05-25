package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.kape.localprefs.Prefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton

private val SHARE_EVENTS_CONSENT = booleanPreferencesKey("share-events-consent")

@Singleton
class ConsentPrefs(
    context: Context,
) : Prefs(context, "consent") {
    val allowSharing: StateFlow<Boolean> =
        getAllowSharing()
            .stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)

    fun setAllowSharing(allow: Boolean) {
        scope.launch {
            dataStore.edit { it[SHARE_EVENTS_CONSENT] = allow }
        }
    }

    private fun getAllowSharing(): Flow<Boolean> = dataStore.data.map { it[SHARE_EVENTS_CONSENT] ?: false }
}