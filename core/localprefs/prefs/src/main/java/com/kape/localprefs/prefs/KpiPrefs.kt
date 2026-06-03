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
import org.koin.core.annotation.Singleton

private val ACTIVE_PROTOCOL = stringPreferencesKey("active-protocol")

@Singleton
class KpiPrefs(
    context: Context,
) : Prefs(context, "kpi") {
    val activeProtocol: StateFlow<String> =
        getActiveProtocol().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), "")

    suspend fun setActiveProtocol(protocol: String) {
        dataStore.edit { it[ACTIVE_PROTOCOL] = protocol }
    }

    private fun getActiveProtocol(): Flow<String> = dataStore.data.map { it[ACTIVE_PROTOCOL] ?: "" }
}