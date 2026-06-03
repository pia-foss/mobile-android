package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.localprefs.Prefs
import com.kape.regions.data.ServerData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

private val SHADOWSOCKS_VPN_FAVORITES = stringPreferencesKey("favorite-servers")
private val SHADOWSOCKS_SELECTED_SERVER = stringPreferencesKey("shadowsocks-selected-server")
private val SHADOWSOCKS_SERVERS = stringPreferencesKey("shadowsocks-servers")

@Singleton
class ShadowsocksRegionPrefs(
    context: Context,
) : Prefs(context, "shadowsocks-regions") {
    val selectedShadowsocksServer: StateFlow<ShadowsocksServer?> =
        getSelectedShadowsocksServer().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), null)
    val shadowsocksServers: StateFlow<List<ShadowsocksServer>> =
        getShadowsocksServers().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), emptyList())

    suspend fun addToFavorites(shadowsocksServerName: String) {
        dataStore.edit { prefs ->
            val favorites =
                prefs[SHADOWSOCKS_VPN_FAVORITES]
                    ?.let { Json.decodeFromString<List<ServerData>>(it) }
                    ?.toMutableList() ?: mutableListOf()
            favorites.add(ServerData(shadowsocksServerName, false))
            prefs[SHADOWSOCKS_VPN_FAVORITES] = Json.encodeToString(favorites)
        }
    }

    suspend fun removeFromFavorites(shadowsocksServerName: String) {
        dataStore.edit { prefs ->
            val favorites =
                prefs[SHADOWSOCKS_VPN_FAVORITES]
                    ?.let { Json.decodeFromString<List<ServerData>>(it) }
                    ?.toMutableList() ?: mutableListOf()
            favorites.remove(ServerData(shadowsocksServerName, false))
            prefs[SHADOWSOCKS_VPN_FAVORITES] = Json.encodeToString(favorites)
        }
    }

    fun isFavorite(shadowsocksServerName: String): Flow<Boolean> =
        dataStore.data.map { prefs ->
            val favorites =
                prefs[SHADOWSOCKS_VPN_FAVORITES]?.let { Json.decodeFromString<List<ServerData>>(it) }
                    ?: emptyList()
            favorites.contains(ServerData(shadowsocksServerName, false))
        }

    suspend fun setSelectShadowsocksServer(shadowsocksServer: ShadowsocksServer) {
        dataStore.edit { it[SHADOWSOCKS_SELECTED_SERVER] = Json.encodeToString(shadowsocksServer) }
    }

    suspend fun setShadowsocksServers(shadowsocksServers: List<ShadowsocksServer>) {
        dataStore.edit { it[SHADOWSOCKS_SERVERS] = Json.encodeToString(shadowsocksServers) }
    }

    private fun getSelectedShadowsocksServer(): Flow<ShadowsocksServer?> =
        dataStore.data.map { prefs ->
            prefs[SHADOWSOCKS_SELECTED_SERVER]?.let { Json.decodeFromString(it) }
        }

    private fun getShadowsocksServers(): Flow<List<ShadowsocksServer>> =
        dataStore.data.map { prefs ->
            prefs[SHADOWSOCKS_SERVERS]?.let { Json.decodeFromString(it) } ?: emptyList()
        }
}