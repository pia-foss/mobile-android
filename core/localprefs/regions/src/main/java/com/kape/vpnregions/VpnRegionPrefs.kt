package com.kape.vpnregions

import android.content.Context
import com.kape.utils.Prefs
import com.kape.utils.server.VpnServer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val VPN_FAVORITES = "favorites"
private const val VPN_SELECTED_SERVER = "selected-server"
private const val VPN_SERVERS = "servers"

class VpnRegionPrefs(context: Context) : Prefs(context, "vpn-regions") {

    fun addToFavorites(vpnServerName: String) {
        val favorites = prefs.getStringSet(VPN_FAVORITES, mutableSetOf())
        favorites!!.add(vpnServerName)
        prefs.edit().putStringSet(VPN_FAVORITES, favorites).apply()
    }

    fun removeFromFavorites(vpnServerName: String) {
        val favourites = prefs.getStringSet(VPN_FAVORITES, mutableSetOf())
        favourites!!.remove(vpnServerName)
        prefs.edit().putStringSet(VPN_FAVORITES, favourites).apply()
    }

    fun isFavorite(vpnServerName: String): Boolean {
        return prefs.getStringSet(VPN_FAVORITES, emptySet())?.contains(vpnServerName) ?: false
    }

    fun getFavoriteVpnServers(): List<String> {
        return prefs.getStringSet(VPN_FAVORITES, emptySet())?.toList() ?: emptyList()
    }

    fun selectVpnServer(vpnServerName: String) {
        prefs.edit().putString(VPN_SELECTED_SERVER, vpnServerName).apply()
    }

    fun getSelectedVpnServerKey() = prefs.getString(VPN_SELECTED_SERVER, "")

    fun setVpnServers(vpnServers: List<VpnServer>) =
        prefs.edit().putString(VPN_SERVERS, Json.encodeToString(vpnServers)).apply()

    fun getVpnServers(): List<VpnServer> =
        prefs.getString(VPN_SERVERS, null)?.let {
            Json.decodeFromString(it)
        } ?: emptyList()
}