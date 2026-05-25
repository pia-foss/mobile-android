package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kape.localprefs.Prefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton

private val LAST_KNOWN_EXCEPTION = stringPreferencesKey("last-known-exception")
private val PROTOCOL_DEBUG_LOGS = stringPreferencesKey("protocol-debug-logs")
private val DEBUG_LOGS = stringPreferencesKey("debug-logs")

@Singleton
class CsiPrefs(
    context: Context,
) : Prefs(context, "csi") {
    val lastKnownException: StateFlow<String> =
        getLastKnownException().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), "")
    val protocolDebugLogs: StateFlow<String> =
        getProtocolDebugLogs().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), "")
    val customDebugLogs: StateFlow<String> =
        getCustomDebugLogs().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), "")

    fun setLastKnownException(value: String) {
        scope.launch {
            dataStore.edit { it[LAST_KNOWN_EXCEPTION] = value }
        }
    }

    fun setProtocolDebugLogs(value: String) {
        scope.launch {
            dataStore.edit { it[PROTOCOL_DEBUG_LOGS] = value }
        }
    }

    fun addCustomDebugLogs(
        value: String,
        isDebugLoggingEnabled: Boolean,
    ) {
        if (isDebugLoggingEnabled) {
            scope.launch {
                dataStore.edit { prefs ->
                    val current = prefs[DEBUG_LOGS] ?: ""
                    prefs[DEBUG_LOGS] = current + value + "\n"
                }
            }
        }
    }

    fun clearCustomDebugLogs() {
        scope.launch {
            dataStore.edit { it[DEBUG_LOGS] = "" }
        }
    }

    private fun getLastKnownException(): Flow<String> = dataStore.data.map { it[LAST_KNOWN_EXCEPTION] ?: "" }

    private fun getProtocolDebugLogs(): Flow<String> = dataStore.data.map { it[PROTOCOL_DEBUG_LOGS] ?: "" }

    private fun getCustomDebugLogs(): Flow<String> = dataStore.data.map { it[DEBUG_LOGS] ?: "" }
}