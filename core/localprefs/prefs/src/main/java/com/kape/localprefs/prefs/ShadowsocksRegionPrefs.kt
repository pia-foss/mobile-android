package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kape.contracts.di.Qualifiers
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.localprefs.Prefs
import com.kape.localprefs.api.ShadowsocksRegionStore
import com.kape.regions.data.ServerData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

private val SHADOWSOCKS_VPN_FAVORITES = stringPreferencesKey("favorite-servers")
private val SHADOWSOCKS_SELECTED_SERVER = stringPreferencesKey("shadowsocks-selected-server")
private val SHADOWSOCKS_SERVERS = stringPreferencesKey("shadowsocks-servers")

@Singleton(binds = [ShadowsocksRegionPrefs::class, ShadowsocksRegionStore::class])
class ShadowsocksRegionPrefs(
    context: Context,
    @Named(Qualifiers.APP_SCOPE) appScope: CoroutineScope,
    @Named(Qualifiers.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
) : Prefs(context, "shadowsocks-regions", appScope, ioDispatcher),
    ShadowsocksRegionStore {
    val shadowsocksServers: StateFlow<List<ShadowsocksServer>> =
        getShadowsocksServers().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), emptyList())

    override suspend fun addToFavorites(shadowsocksServerName: String) =
        addToFavorites(SHADOWSOCKS_VPN_FAVORITES, ServerData(shadowsocksServerName, false))

    override suspend fun removeFromFavorites(shadowsocksServerName: String) =
        removeFromFavorites(SHADOWSOCKS_VPN_FAVORITES, ServerData(shadowsocksServerName, false))

    override fun observeFavorites(): Flow<List<String>> =
        getFavorites(SHADOWSOCKS_VPN_FAVORITES).map { favorites -> favorites.map { it.name } }

    override fun isFavorite(shadowsocksServerName: String): Flow<Boolean> =
        isFavorite(SHADOWSOCKS_VPN_FAVORITES, ServerData(shadowsocksServerName, false))

    suspend fun getSelectedShadowsocksServerNow(): ShadowsocksServer? = getSelectedShadowsocksServer().first()

    suspend fun getShadowsocksServersNow(): List<ShadowsocksServer> = getShadowsocksServers().first()

    suspend fun setSelectShadowsocksServer(shadowsocksServer: ShadowsocksServer) {
        dataStore.edit { it[SHADOWSOCKS_SELECTED_SERVER] = Json.encodeToString(shadowsocksServer) }
    }

    suspend fun setShadowsocksServers(shadowsocksServers: List<ShadowsocksServer>) {
        dataStore.edit { it[SHADOWSOCKS_SERVERS] = Json.encodeToString(shadowsocksServers) }
    }

    fun getSelectedShadowsocksServer(): Flow<ShadowsocksServer?> =
        dataStore.data.map { prefs ->
            prefs[SHADOWSOCKS_SELECTED_SERVER]?.let { Json.decodeFromString(it) }
        }

    fun getShadowsocksServers(): Flow<List<ShadowsocksServer>> =
        dataStore.data.map { prefs ->
            prefs[SHADOWSOCKS_SERVERS]?.let { Json.decodeFromString(it) } ?: emptyList()
        }
}