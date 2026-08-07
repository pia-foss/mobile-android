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
import org.koin.core.annotation.Singleton

private val SHARE_EVENTS_CONSENT = booleanPreferencesKey("share-events-consent")
private val CONSENT_DECISION_MADE = booleanPreferencesKey("consent-decision-made")

@Singleton
class ConsentPrefs(
    context: Context,
) : Prefs(context, "consent") {
    val allowSharing: StateFlow<Boolean> =
        getAllowSharing()
            .stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)

    // Persisted (not just in-memory) so it survives process death - otherwise a user killed
    // mid-signup (after accepting consent but before finishing) is sent back through the
    // consent screen on relaunch instead of resuming where they left off. Cleared on logout
    // via Prefs.clear().
    val hasMadeConsentDecision: StateFlow<Boolean> =
        getConsentDecisionMade()
            .stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)

    suspend fun setAllowSharing(allow: Boolean) {
        dataStore.edit { it[SHARE_EVENTS_CONSENT] = allow }
    }

    suspend fun setConsentDecisionMade(made: Boolean) {
        dataStore.edit { it[CONSENT_DECISION_MADE] = made }
    }

    private fun getAllowSharing(): Flow<Boolean> = dataStore.data.map { it[SHARE_EVENTS_CONSENT] ?: false }

    private fun getConsentDecisionMade(): Flow<Boolean> = dataStore.data.map { it[CONSENT_DECISION_MADE] ?: false }
}