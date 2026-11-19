package com.kape.regions.shadowsocks.data.local

import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Singleton

internal interface ShadowsocksRegionLocalDataSource {
    fun observeServers(): Flow<List<ShadowsocksServer>>

    suspend fun saveServers(servers: List<ShadowsocksServer>)

    fun observeSelectedServer(): Flow<ShadowsocksServer?>

    suspend fun saveSelectedServer(server: ShadowsocksServer)
}

@Singleton(binds = [ShadowsocksRegionLocalDataSource::class])
internal class PrefsShadowsocksRegionLocalDataSource(
    private val prefs: ShadowsocksRegionPrefs,
) : ShadowsocksRegionLocalDataSource {
    override fun observeServers(): Flow<List<ShadowsocksServer>> = prefs.getShadowsocksServers()

    override suspend fun saveServers(servers: List<ShadowsocksServer>) = prefs.setShadowsocksServers(servers)

    override fun observeSelectedServer(): Flow<ShadowsocksServer?> = prefs.getSelectedShadowsocksServer()

    override suspend fun saveSelectedServer(server: ShadowsocksServer) = prefs.setSelectShadowsocksServer(server)
}