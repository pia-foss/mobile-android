package com.kape.localprefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import com.kape.regions.data.ServerData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

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

    protected suspend fun addToFavorites(
        key: Preferences.Key<String>,
        entry: ServerData,
    ) {
        dataStore.edit { prefs ->
            val favorites = prefs.decodeFavorites(key).toMutableList()
            favorites.add(entry)
            prefs[key] = Json.encodeToString(favorites)
        }
    }

    protected suspend fun removeFromFavorites(
        key: Preferences.Key<String>,
        entry: ServerData,
    ) {
        dataStore.edit { prefs ->
            val favorites = prefs.decodeFavorites(key).toMutableList()
            favorites.remove(entry)
            prefs[key] = Json.encodeToString(favorites)
        }
    }

    protected fun isFavorite(
        key: Preferences.Key<String>,
        entry: ServerData,
    ): Flow<Boolean> = getFavorites(key).map { it.contains(entry) }

    protected fun getFavorites(key: Preferences.Key<String>): Flow<List<ServerData>> =
        dataStore.data.map { prefs -> prefs.decodeFavorites(key) }

    private fun Preferences.decodeFavorites(key: Preferences.Key<String>): List<ServerData> =
        this[key]?.let { Json.decodeFromString<List<ServerData>>(it) } ?: emptyList()
}