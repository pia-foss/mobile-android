package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kape.contracts.di.Qualifiers
import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServerOutdated
import com.kape.localprefs.Prefs
import com.kape.localprefs.api.VpnRegionStore
import com.kape.regions.data.ServerData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

private val VPN_FAVORITES = stringPreferencesKey("favorites")
private val VPN_SELECTED_SERVER = stringPreferencesKey("selected-vpn-server")
private val VPN_RECONNECT = booleanPreferencesKey("reconnect")

@Singleton(binds = [VpnRegionPrefs::class, VpnRegionStore::class])
class VpnRegionPrefs(
    context: Context,
    @Named(Qualifiers.APP_SCOPE) appScope: CoroutineScope,
    @Named(Qualifiers.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
) : Prefs(context, "vpn-regions", appScope, ioDispatcher),
    VpnRegionStore {
    override val favoriteVpnServers: StateFlow<List<ServerData>> =
        getFavorites(VPN_FAVORITES).stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            emptyList(),
        )
    val selectedServer: StateFlow<VpnServer?> =
        getSelectedServer().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), null)
    val needsVpnReconnect: StateFlow<Boolean> =
        getNeedsVpnReconnect().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)

    override suspend fun addToFavorites(serverData: ServerData) = addToFavorites(VPN_FAVORITES, serverData)

    override suspend fun removeFromFavorites(serverData: ServerData) = removeFromFavorites(VPN_FAVORITES, serverData)

    override fun isFavorite(serverData: ServerData): Flow<Boolean> = isFavorite(VPN_FAVORITES, serverData)

    fun isFavorite(
        serverName: String,
        isDip: Boolean,
    ): Flow<Boolean> = isFavorite(ServerData(serverName, isDip))

    override suspend fun selectVpnServer(vpnServer: VpnServer) {
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