package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kape.connection.model.PortBindInformation
import com.kape.connection.model.QuickConnectServer
import com.kape.data.NO_IP
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.Prefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

private val QUICK_CONNECT = stringPreferencesKey("quick-connect-server-list")
private val CLIENT_IP = stringPreferencesKey("client-ip")
private val VPN_IP = stringPreferencesKey("vpn-ip")
private val PRE_SELECTED_VPN_SERVER = stringPreferencesKey("pre-selected-vpn-server")
private val LAST_SNOOZE_END_TIME = longPreferencesKey("last-snooze-end-time")
private val GATEWAY = stringPreferencesKey("gateway")
private val PORT_BINDING_INFO = stringPreferencesKey("port-binding-info")
private val DISCONNECTED_BY_USER = booleanPreferencesKey("disconnected-by-user")
private val PROXY_PORT = stringPreferencesKey("proxy-port")
private const val DEFAULT_PROXY_PORT_VALUE = "8080"

@Singleton
class ConnectionPrefs(
    context: Context,
) : Prefs(context, "connection") {
    val quickConnectServers: StateFlow<List<QuickConnectServer>> =
        getQuickConnectServers().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            emptyList(),
        )
    val clientIp: StateFlow<String> =
        getClientIp().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), NO_IP)
    val vpnIp: StateFlow<String> =
        getVpnIp().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), NO_IP)
    val selectedVpnServer: StateFlow<VpnServer?> =
        getSelectedVpnServer().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), null)
    val lastSnoozeEndTime: StateFlow<Long> =
        getLastSnoozeEndTime().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), 0L)
    val gateway: StateFlow<String> =
        getGateway().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), "")
    val portBindingInfo: StateFlow<PortBindInformation?> =
        getPortBindingInfo().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), null)
    val isDisconnectedByUser: StateFlow<Boolean> =
        getDisconnectedByUser().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    val proxyPort: StateFlow<String> =
        getProxyPort().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            DEFAULT_PROXY_PORT_VALUE,
        )

    suspend fun addToQuickConnect(
        serverKey: String,
        isDip: Boolean,
    ) {
        dataStore.edit { prefs ->
            val list =
                prefs[QUICK_CONNECT]
                    ?.let { Json.decodeFromString<List<QuickConnectServer>>(it) }
                    ?.toMutableList() ?: mutableListOf()
            list
                .firstOrNull { it.serverKey == serverKey && it.isDip == isDip }
                ?.let { list.remove(it) }
            list.add(QuickConnectServer(serverKey, isDip))
            prefs[QUICK_CONNECT] = Json.encodeToString(list)
        }
    }

    suspend fun removeFromQuickConnect(serverKey: String) {
        dataStore.edit { prefs ->
            val list =
                prefs[QUICK_CONNECT]
                    ?.let { Json.decodeFromString<List<QuickConnectServer>>(it) }
                    ?.toMutableList() ?: mutableListOf()
            list.firstOrNull { it.serverKey == serverKey && it.isDip }?.let { list.remove(it) }
            prefs[QUICK_CONNECT] = Json.encodeToString(list)
        }
    }

    suspend fun setClientIp(ip: String) {
        dataStore.edit { it[CLIENT_IP] = ip }
    }

    suspend fun setVpnIp(vpnIp: String) {
        dataStore.edit { it[VPN_IP] = vpnIp }
    }

    suspend fun setSelectedVpnServer(server: VpnServer?) {
        dataStore.edit { it[PRE_SELECTED_VPN_SERVER] = Json.encodeToString(server) }
    }

    suspend fun setLastSnoozeEndTime(endTime: Long) {
        dataStore.edit { it[LAST_SNOOZE_END_TIME] = endTime }
    }

    suspend fun setGateway(gateway: String) {
        dataStore.edit { it[GATEWAY] = gateway }
    }

    suspend fun clearGateway() = setGateway("")

    suspend fun setPortBindingInformation(info: PortBindInformation?) {
        dataStore.edit { it[PORT_BINDING_INFO] = Json.encodeToString(info) }
    }

    suspend fun clearPortBindingInfo() = setPortBindingInformation(null)

    suspend fun setDisconnectedByUser(byUser: Boolean) {
        dataStore.edit { it[DISCONNECTED_BY_USER] = byUser }
    }

    suspend fun setProxyPort(port: String?) {
        dataStore.edit { prefs ->
            if (port != null) prefs[PROXY_PORT] = port else prefs.remove(PROXY_PORT)
        }
    }

    private fun getQuickConnectServers(): Flow<List<QuickConnectServer>> =
        dataStore.data.map { prefs ->
            prefs[QUICK_CONNECT]?.let { Json.decodeFromString(it) } ?: emptyList()
        }

    private fun getClientIp(): Flow<String> = dataStore.data.map { it[CLIENT_IP] ?: NO_IP }

    private fun getVpnIp(): Flow<String> = dataStore.data.map { it[VPN_IP] ?: NO_IP }

    private fun getSelectedVpnServer(): Flow<VpnServer?> =
        dataStore.data.map { prefs ->
            prefs[PRE_SELECTED_VPN_SERVER]?.let { Json.decodeFromString(it) }
        }

    private fun getLastSnoozeEndTime(): Flow<Long> = dataStore.data.map { it[LAST_SNOOZE_END_TIME] ?: 0L }

    private fun getGateway(): Flow<String> = dataStore.data.map { it[GATEWAY] ?: "" }

    private fun getPortBindingInfo(): Flow<PortBindInformation?> =
        dataStore.data.map { prefs ->
            prefs[PORT_BINDING_INFO]?.let { Json.decodeFromString(it) }
        }

    private fun getDisconnectedByUser(): Flow<Boolean> = dataStore.data.map { it[DISCONNECTED_BY_USER] ?: false }

    private fun getProxyPort(): Flow<String> = dataStore.data.map { it[PROXY_PORT] ?: DEFAULT_PROXY_PORT_VALUE }
}