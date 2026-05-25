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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
    fun addToQuickConnect(
        serverKey: String,
        isDip: Boolean,
    ) {
        scope.launch {
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
    }

    fun removeFromQuickConnect(serverKey: String) {
        scope.launch {
            dataStore.edit { prefs ->
                val list =
                    prefs[QUICK_CONNECT]
                        ?.let { Json.decodeFromString<List<QuickConnectServer>>(it) }
                        ?.toMutableList() ?: mutableListOf()
                list.firstOrNull { it.serverKey == serverKey && it.isDip }?.let { list.remove(it) }
                prefs[QUICK_CONNECT] = Json.encodeToString(list)
            }
        }
    }

    fun getQuickConnectServers(): Flow<List<QuickConnectServer>> =
        dataStore.data.map { prefs ->
            prefs[QUICK_CONNECT]?.let { Json.decodeFromString(it) } ?: emptyList()
        }

    fun setClientIp(ip: String) {
        scope.launch {
            dataStore.edit { it[CLIENT_IP] = ip }
        }
    }

    fun getClientIp(): Flow<String> = dataStore.data.map { it[CLIENT_IP] ?: NO_IP }

    fun setVpnIp(vpnIp: String) {
        scope.launch {
            dataStore.edit { it[VPN_IP] = vpnIp }
        }
    }

    fun getVpnIp(): Flow<String> = dataStore.data.map { it[VPN_IP] ?: NO_IP }

    fun setSelectedVpnServer(server: VpnServer?) {
        scope.launch {
            dataStore.edit { it[PRE_SELECTED_VPN_SERVER] = Json.encodeToString(server) }
        }
    }

    fun getSelectedVpnServer(): Flow<VpnServer?> =
        dataStore.data.map { prefs ->
            prefs[PRE_SELECTED_VPN_SERVER]?.let { Json.decodeFromString(it) }
        }

    fun setLastSnoozeEndTime(endTime: Long) {
        scope.launch {
            dataStore.edit { it[LAST_SNOOZE_END_TIME] = endTime }
        }
    }

    fun getLastSnoozeEndTime(): Flow<Long> = dataStore.data.map { it[LAST_SNOOZE_END_TIME] ?: 0L }

    fun setGateway(gateway: String) {
        scope.launch {
            dataStore.edit { it[GATEWAY] = gateway }
        }
    }

    fun getGateway(): Flow<String> = dataStore.data.map { it[GATEWAY] ?: "" }

    fun clearGateway() = setGateway("")

    fun setPortBindingInformation(info: PortBindInformation?) {
        scope.launch {
            dataStore.edit { it[PORT_BINDING_INFO] = Json.encodeToString(info) }
        }
    }

    fun getPortBindingInfo(): Flow<PortBindInformation?> =
        dataStore.data.map { prefs ->
            prefs[PORT_BINDING_INFO]?.let { Json.decodeFromString(it) }
        }

    fun clearPortBindingInfo() = setPortBindingInformation(null)

    fun disconnectedByUser(byUser: Boolean) {
        scope.launch {
            dataStore.edit { it[DISCONNECTED_BY_USER] = byUser }
        }
    }

    fun isDisconnectedByUser(): Flow<Boolean> = dataStore.data.map { it[DISCONNECTED_BY_USER] ?: false }

    fun setProxyPort(port: String?) {
        scope.launch {
            dataStore.edit { prefs ->
                if (port != null) prefs[PROXY_PORT] = port else prefs.remove(PROXY_PORT)
            }
        }
    }

    fun getProxyPort(): Flow<String> = dataStore.data.map { it[PROXY_PORT] ?: DEFAULT_PROXY_PORT_VALUE }
}