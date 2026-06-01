package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServerOutdated
import com.kape.localprefs.Prefs
import com.kape.regions.data.ServerData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

private val VPN_FAVORITES = stringPreferencesKey("favorites")
private val VPN_SELECTED_SERVER = stringPreferencesKey("selected-vpn-server")
private val VPN_RECONNECT = booleanPreferencesKey("reconnect")

@Singleton
class VpnRegionPrefs(
    context: Context,
) : Prefs(context, "vpn-regions") {
    val favoriteVpnServers: StateFlow<List<ServerData>> =
        getFavoriteVpnServers().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            emptyList(),
        )
    val selectedServer: StateFlow<VpnServer?> =
        getSelectedServer().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), null)
    val needsVpnReconnect: StateFlow<Boolean> =
        getNeedsVpnReconnect().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)

    fun addToFavorites(serverData: ServerData) {
        scope.launch {
            dataStore.edit { prefs ->
                val favorites =
                    prefs[VPN_FAVORITES]?.let { Json.decodeFromString<List<ServerData>>(it) }
                        ?: emptyList()
                favorites.toMutableList().add(serverData)
                prefs[VPN_FAVORITES] = Json.encodeToString(favorites)
            }
        }
    }

    fun removeFromFavorites(serverData: ServerData) {
        scope.launch {
            dataStore.edit { prefs ->
                val favorites =
                    prefs[VPN_FAVORITES]
                        ?.let { Json.decodeFromString<List<ServerData>>(it) }
                        ?.toMutableList() ?: mutableListOf()
                favorites.remove(serverData)
                prefs[VPN_FAVORITES] = Json.encodeToString(favorites)
            }
        }
    }

    fun isFavorite(serverData: ServerData): Flow<Boolean> =
        dataStore.data.map { prefs ->
            val favorites =
                prefs[VPN_FAVORITES]?.let { Json.decodeFromString<List<ServerData>>(it) }
                    ?: emptyList()
            favorites.contains(serverData)
        }

    fun isFavorite(
        serverName: String,
        isDip: Boolean,
    ): Flow<Boolean> = isFavorite(ServerData(serverName, isDip))

    fun selectVpnServer(vpnServer: VpnServer) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[VPN_SELECTED_SERVER] = Json.encodeToString(vpnServer)
                prefs[VPN_RECONNECT] = true
            }
        }
    }

    fun setVpnReconnect(needsReconnect: Boolean) {
        scope.launch {
            dataStore.edit { it[VPN_RECONNECT] = needsReconnect }
        }
    }

    private fun getFavoriteVpnServers(): Flow<List<ServerData>> =
        dataStore.data.map { prefs ->
            prefs[VPN_FAVORITES]?.let { Json.decodeFromString(it) } ?: emptyList()
        }

    @OptIn(ExperimentalSerializationApi::class)
    private fun getSelectedServer(): Flow<VpnServer?> =
        dataStore.data.map { prefs ->
            try {
                prefs[VPN_SELECTED_SERVER]?.let { Json.decodeFromString<VpnServer>(it) }
            } catch (exception: MissingFieldException) {
                prefs[VPN_SELECTED_SERVER]?.let {
                    Json.decodeFromString<VpnServerOutdated>(it).toVpnServer()
                }
            }
        }

    private fun getNeedsVpnReconnect(): Flow<Boolean> = dataStore.data.map { it[VPN_RECONNECT] ?: false }
}