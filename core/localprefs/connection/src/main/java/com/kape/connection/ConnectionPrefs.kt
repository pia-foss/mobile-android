package com.kape.connection

import android.content.Context
import com.kape.connection.model.PortBindInformation
import com.kape.utils.Prefs
import com.kape.utils.server.Server
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val QUICK_CONNECT = "quick-connect-list"
private const val CLIENT_IP = "client-ip"
private const val CLIENT_VPN_IP = "client-vpn-ip"
private const val SELECTED_SERVER = "selected-server"
private const val LAST_SNOOZE_END_TIME = "last-snooze-end-time"
private const val GATEWAY = "gateway"
private const val PORT_BINDING_INFO = "port-binding-info"
const val NO_IP = "---"

class ConnectionPrefs(context: Context) : Prefs(context, "connection") {

    fun addToQuickConnect(serverKey: String) {
        val quickConnectList =
            prefs.getStringSet(QUICK_CONNECT, mutableSetOf())?.toMutableList() ?: mutableListOf()
        quickConnectList.add(serverKey)
        prefs.edit().putStringSet(QUICK_CONNECT, quickConnectList.toSet()).apply()
    }

    fun getQuickConnectServers(): List<String> {
        return prefs.getStringSet(QUICK_CONNECT, emptySet())?.toList() ?: emptyList()
    }

    fun setClientIp(ip: String) = prefs.edit().putString(CLIENT_IP, ip).apply()

    fun getClientIp(): String = prefs.getString(CLIENT_IP, NO_IP) ?: NO_IP

    fun setClientVpnIp(vpnIp: String) = prefs.edit().putString(CLIENT_VPN_IP, vpnIp).apply()

    fun getClientVpnIp() = prefs.getString(CLIENT_VPN_IP, NO_IP) ?: NO_IP

    fun setSelectedServer(server: Server) =
        prefs.edit().putString(SELECTED_SERVER, Json.encodeToString(server)).apply()

    fun getSelectedServer(): Server? = prefs.getString(SELECTED_SERVER, null)?.let {
        Json.decodeFromString(it)
    }

    fun setLastSnoozeEndTime(endTime: Long) =
        prefs.edit().putLong(LAST_SNOOZE_END_TIME, endTime).apply()

    fun getLastSnoozeEndTime() = prefs.getLong(LAST_SNOOZE_END_TIME, 0)

    fun setGateway(gateway: String) = prefs.edit().putString(GATEWAY, gateway).apply()

    fun getGateway() = prefs.getString(GATEWAY, "") ?: ""

    fun clearGateway() = setGateway("")

    fun setPortBindingInformation(info: PortBindInformation?) {
        prefs.edit().putString(PORT_BINDING_INFO, Json.encodeToString(info)).apply()
    }

    fun getPortBindingInfo(): PortBindInformation? = prefs.getString(PORT_BINDING_INFO, null)?.let {
        Json.decodeFromString(it)
    }

    fun clearPortBindingInfo() = setPortBindingInformation(null)
}