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
        getFavorites(VPN_FAVORITES).stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            emptyList(),
        )
    val selectedServer: StateFlow<VpnServer?> =
        getSelectedServer().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), null)
    val needsVpnReconnect: StateFlow<Boolean> =
        getNeedsVpnReconnect().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)

    suspend fun addToFavorites(serverData: ServerData) = addToFavorites(VPN_FAVORITES, serverData)

    suspend fun removeFromFavorites(serverData: ServerData) = removeFromFavorites(VPN_FAVORITES, serverData)

    fun isFavorite(serverData: ServerData): Flow<Boolean> = isFavorite(VPN_FAVORITES, serverData)

    fun isFavorite(
        serverName: String,
        isDip: Boolean,
    ): Flow<Boolean> = isFavorite(ServerData(serverName, isDip))

    suspend fun selectVpnServer(vpnServer: VpnServer) {
        dataStore.edit { prefs ->
            prefs[VPN_SELECTED_SERVER] = Json.encodeToString(vpnServer)
            prefs[VPN_RECONNECT] = true
        }
    }

    suspend fun setVpnReconnect(needsReconnect: Boolean) {
        dataStore.edit { it[VPN_RECONNECT] = needsReconnect }
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