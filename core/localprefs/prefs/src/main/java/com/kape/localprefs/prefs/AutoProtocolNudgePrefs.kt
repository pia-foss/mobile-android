package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kape.contracts.di.Qualifiers
import com.kape.localprefs.Prefs
import com.kape.localprefs.api.AutoProtocolNudgeStore
import com.kape.vpnconnect.data.AutoProtocolNudgeState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

private val AUTO_PROTOCOL_NUDGE_STATE = stringPreferencesKey("auto-protocol-nudge-state")

@Singleton(binds = [AutoProtocolNudgePrefs::class, AutoProtocolNudgeStore::class])
class AutoProtocolNudgePrefs(
    context: Context,
    @Named(Qualifiers.APP_SCOPE) appScope: CoroutineScope,
    @Named(Qualifiers.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
) : Prefs(context, "auto-protocol-nudge", appScope, ioDispatcher),
    AutoProtocolNudgeStore {
    val state: StateFlow<AutoProtocolNudgeState> =
        getState().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), AutoProtocolNudgeState())

    override suspend fun setState(state: AutoProtocolNudgeState) {
        dataStore.edit { it[AUTO_PROTOCOL_NUDGE_STATE] = Json.encodeToString(state) }
    }

    override suspend fun getStateNow(): AutoProtocolNudgeState = getState().first()

    private fun getState(): Flow<AutoProtocolNudgeState> =
        dataStore.data.map { prefs ->
            prefs[AUTO_PROTOCOL_NUDGE_STATE]?.let { Json.decodeFromString(it) } ?: AutoProtocolNudgeState()
        }
}