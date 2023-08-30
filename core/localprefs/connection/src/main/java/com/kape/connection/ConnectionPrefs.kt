package com.kape.connection

import android.content.Context
import com.kape.utils.Prefs

private const val QUICK_CONNECT = "quick-connect-list"
private const val CLIENT_IP = "client-ip"
private const val CLIENT_VPN_IP = "client-vpn-ip"
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
}