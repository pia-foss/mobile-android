package com.kape.shadowsocksregions

import android.content.Context
import com.kape.utils.Prefs
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val SHADOWSOCKS_SELECTED_SERVER = "shadowsocks-selected-server"
private const val SHADOWSOCKS_SERVERS = "shadowsocks-servers"

class ShadowsocksRegionPrefs(context: Context) : Prefs(context, "shadowsocks-regions") {

    fun setSelectShadowsocksServer(shadowsocksServerName: String) =
        prefs.edit().putString(SHADOWSOCKS_SELECTED_SERVER, shadowsocksServerName).apply()

    fun getSelectedShadowsocksServerKey() =
        prefs.getString(SHADOWSOCKS_SELECTED_SERVER, "")

    fun setShadowsocksServers(shadowsocksServers: List<ShadowsocksServer>) =
        prefs.edit().putString(SHADOWSOCKS_SERVERS, Json.encodeToString(shadowsocksServers)).apply()

    fun getShadowsocksServers(): List<ShadowsocksServer> =
        prefs.getString(SHADOWSOCKS_SERVERS, null)?.let {
            Json.decodeFromString(it)
        } ?: emptyList()
}