package com.kape.localprefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

open class Prefs(
    context: Context,
    name: String,
) {
    protected val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val waitTime = 5000L

    val dataStore: DataStore<Preferences> =
        DataStoreFactory.create(
            serializer = EncryptedPreferencesSerializer(context, name),
            produceFile = { context.preferencesDataStoreFile(name) },
            migrations = listOf(EncryptedSharedPrefsMigration(context, name)),
            scope = scope,
        )

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}