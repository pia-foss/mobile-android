package com.kape.regions.shadowsocks.data.bundled

import com.kape.contracts.di.Qualifiers
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.regions.common.data.AssetReader
import com.kape.regions.common.data.withoutSignature
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

internal interface BundledShadowsocksRegionDataSource {
    suspend fun getServers(): List<ShadowsocksServer>
}

@Singleton(binds = [BundledShadowsocksRegionDataSource::class])
internal class AssetBundledShadowsocksRegionDataSource(
    private val assetReader: AssetReader,
    @Named(Qualifiers.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) : BundledShadowsocksRegionDataSource {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var cache: List<ShadowsocksServer>? = null

    override suspend fun getServers(): List<ShadowsocksServer> =
        mutex.withLock {
            cache ?: withContext(ioDispatcher) { read() }.also { cache = it }
        }

    private fun read(): List<ShadowsocksServer> =
        json
            .decodeFromString<List<BundledShadowsocksRegionDto>>(
                assetReader.read(SHADOWSOCKS_REGIONS_FILE).withoutSignature(),
            ).map { it.toDomain() }

    @Serializable
    private data class BundledShadowsocksRegionDto(
        val region: String,
        val host: String,
        val port: Int,
        val key: String,
        val cipher: String,
    ) {
        fun toDomain() = ShadowsocksServer(region = region, host = host, port = port, key = key, cipher = cipher)
    }

    private companion object {
        const val SHADOWSOCKS_REGIONS_FILE = "shadowsocks-regions.json"
    }
}