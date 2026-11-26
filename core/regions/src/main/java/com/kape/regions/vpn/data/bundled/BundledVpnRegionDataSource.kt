package com.kape.regions.vpn.data.bundled

import com.kape.contracts.di.Qualifiers
import com.kape.regions.common.data.AssetReader
import com.kape.regions.common.data.withoutSignature
import com.kape.regions.vpn.data.VpnRegions
import com.kape.regions.vpn.data.toVpnRegions
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

internal interface BundledVpnRegionDataSource {
    suspend fun getRegions(): VpnRegions
}

@Singleton(binds = [BundledVpnRegionDataSource::class])
internal class AssetBundledVpnRegionDataSource(
    private val assetReader: AssetReader,
    @Named(Qualifiers.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) : BundledVpnRegionDataSource {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var cache: VpnRegions? = null

    override suspend fun getRegions(): VpnRegions =
        mutex.withLock {
            cache ?: withContext(ioDispatcher) { read() }.also { cache = it }
        }

    private fun read(): VpnRegions =
        json
            .decodeFromString<VpnRegionsResponse>(assetReader.read(VPN_REGIONS_FILE).withoutSignature())
            .toVpnRegions(isBundled = true)

    private companion object {
        const val VPN_REGIONS_FILE = "vpn-regions.json"
    }
}