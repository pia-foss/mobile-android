package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.kape.contracts.di.Qualifiers
import com.kape.localprefs.Prefs
import com.kape.localprefs.api.ConsentStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

private val SHARE_EVENTS_CONSENT = booleanPreferencesKey("share-events-consent")

@Singleton(binds = [ConsentPrefs::class, ConsentStore::class])
class ConsentPrefs(
    context: Context,
    @Named(Qualifiers.APP_SCOPE) appScope: CoroutineScope,
    @Named(Qualifiers.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
) : Prefs(context, "consent", appScope, ioDispatcher),
    ConsentStore {
    override val allowSharing: StateFlow<Boolean> =
        getAllowSharing()
            .stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)

    override suspend fun setAllowSharing(allow: Boolean) {
        dataStore.edit { it[SHARE_EVENTS_CONSENT] = allow }
    }

    // The key is written by any answer and removed on logout, so its presence is the persisted decision.
    suspend fun hasMadeConsentDecisionNow(): Boolean = dataStore.data.first().contains(SHARE_EVENTS_CONSENT)

    suspend fun clearAllowSharing() {
        dataStore.edit { it.remove(SHARE_EVENTS_CONSENT) }
    }

    private fun getAllowSharing(): Flow<Boolean> = dataStore.data.map { it[SHARE_EVENTS_CONSENT] ?: false }
}