package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kape.localprefs.Prefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton

private val ACTIVE_PROTOCOL = stringPreferencesKey("active-protocol")

@Singleton
class KpiPrefs(
    context: Context,
) : Prefs(context, "kpi") {
    fun setActiveProtocol(protocol: String) {
        scope.launch {
            dataStore.edit { it[ACTIVE_PROTOCOL] = protocol }
        }
    }

    fun getActiveProtocol(): Flow<String> = dataStore.data.map { it[ACTIVE_PROTOCOL] ?: "" }
}