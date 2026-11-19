package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kape.contracts.di.Qualifiers
import com.kape.localprefs.Prefs
import com.kape.localprefs.api.CsiStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

private val LAST_KNOWN_EXCEPTION = stringPreferencesKey("last-known-exception")
private val PROTOCOL_DEBUG_LOGS = stringPreferencesKey("protocol-debug-logs")

@Singleton(binds = [CsiPrefs::class, CsiStore::class])
class CsiPrefs(
    context: Context,
    @Named(Qualifiers.APP_SCOPE) appScope: CoroutineScope,
    @Named(Qualifiers.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
) : Prefs(context, "csi", appScope, ioDispatcher),
    CsiStore {
    val lastKnownException: StateFlow<String> =
        getLastKnownException().stateIn(scope, SharingStarted.Eagerly, "")
    val protocolDebugLogs: StateFlow<String> =
        getProtocolDebugLogs().stateIn(scope, SharingStarted.Eagerly, "")

    suspend fun setLastKnownException(value: String) {
        dataStore.edit { it[LAST_KNOWN_EXCEPTION] = value }
    }

    override suspend fun setProtocolDebugLogs(value: String) {
        dataStore.edit { it[PROTOCOL_DEBUG_LOGS] = value }
    }

    override suspend fun clearProtocolDebugLogs() {
        dataStore.edit { it[PROTOCOL_DEBUG_LOGS] = "" }
    }

    suspend fun getLastKnownExceptionNow(): String = getLastKnownException().first()

    suspend fun getProtocolDebugLogsNow(): String = getProtocolDebugLogs().first()

    private fun getLastKnownException(): Flow<String> = dataStore.data.map { it[LAST_KNOWN_EXCEPTION] ?: "" }

    private fun getProtocolDebugLogs(): Flow<String> = dataStore.data.map { it[PROTOCOL_DEBUG_LOGS] ?: "" }
}