package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.localprefs.Prefs
import com.kape.regions.data.ServerData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

private val SHADOWSOCKS_VPN_FAVORITES = stringPreferencesKey("favorite-servers")
private val SHADOWSOCKS_SELECTED_SERVER = stringPreferencesKey("shadowsocks-selected-server")
private val SHADOWSOCKS_SERVERS = stringPreferencesKey("shadowsocks-servers")

@Singleton
class ShadowsocksRegionPrefs(
    context: Context,
) : Prefs(context, "shadowsocks-regions") {
    fun addToFavorites(shadowsocksServerName: String) {
        scope.launch {
            dataStore.edit { prefs ->
                val favorites =
                    prefs[SHADOWSOCKS_VPN_FAVORITES]
                        ?.let { Json.decodeFromString<List<ServerData>>(it) }
                        ?.toMutableList() ?: mutableListOf()
                favorites.add(ServerData(shadowsocksServerName, false))
                prefs[SHADOWSOCKS_VPN_FAVORITES] = Json.encodeToString(favorites)
            }
        }
    }

    fun removeFromFavorites(shadowsocksServerName: String) {
        scope.launch {
            dataStore.edit { prefs ->
                val favorites =
                    prefs[SHADOWSOCKS_VPN_FAVORITES]
                        ?.let { Json.decodeFromString<List<ServerData>>(it) }
                        ?.toMutableList() ?: mutableListOf()
                favorites.remove(ServerData(shadowsocksServerName, false))
                prefs[SHADOWSOCKS_VPN_FAVORITES] = Json.encodeToString(favorites)
            }
        }
    }

    fun isFavorite(shadowsocksServerName: String): Flow<Boolean> =
        dataStore.data.map { prefs ->
            val favorites =
                prefs[SHADOWSOCKS_VPN_FAVORITES]?.let { Json.decodeFromString<List<ServerData>>(it) }
                    ?: emptyList()
            favorites.contains(ServerData(shadowsocksServerName, false))
        }

    fun setSelectShadowsocksServer(shadowsocksServer: ShadowsocksServer) {
        scope.launch {
            dataStore.edit { it[SHADOWSOCKS_SELECTED_SERVER] = Json.encodeToString(shadowsocksServer) }
        }
    }

    fun getSelectedShadowsocksServer(): Flow<ShadowsocksServer?> =
        dataStore.data.map { prefs ->
            prefs[SHADOWSOCKS_SELECTED_SERVER]?.let { Json.decodeFromString(it) }
        }

    fun setShadowsocksServers(shadowsocksServers: List<ShadowsocksServer>) {
        scope.launch {
            dataStore.edit { it[SHADOWSOCKS_SERVERS] = Json.encodeToString(shadowsocksServers) }
        }
    }

    fun getShadowsocksServers(): Flow<List<ShadowsocksServer>> =
        dataStore.data.map { prefs ->
            prefs[SHADOWSOCKS_SERVERS]?.let { Json.decodeFromString(it) } ?: emptyList()
        }
}