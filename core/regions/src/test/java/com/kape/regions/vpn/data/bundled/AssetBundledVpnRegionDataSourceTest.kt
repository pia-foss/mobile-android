package com.kape.regions.vpn.data.bundled

import com.kape.data.vpnserver.VpnServer.ServerGroup
import com.kape.regions.common.data.AssetReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class AssetBundledVpnRegionDataSourceTest {
    private val assets = File("../../app/src/main/assets")
    private val dataSource =
        AssetBundledVpnRegionDataSource(AssetReader { File(assets, it).readText() }, Dispatchers.Unconfined)

    private val reads = mutableListOf<String>()
    private var content = SIGNED_REGIONS
    private val fakeAssetDataSource =
        AssetBundledVpnRegionDataSource(
            AssetReader {
                reads += it
                content
            },
            Dispatchers.Unconfined,
        )

    @Test
    fun `parses the bundled regions`() =
        runTest {
            val regions = dataSource.getRegions()

            assertTrue(regions.isBundled)
            assertTrue(regions.servers.size > 50)
            assertTrue(regions.servers.all { it.endpoints[ServerGroup.META].orEmpty().isNotEmpty() })
            assertTrue(
                regions.servers
                    .flatMap { it.endpoints[ServerGroup.WIREGUARD].orEmpty() }
                    .all { it.ip.contains(':') },
            )
            assertTrue(regions.info.isValid())
        }

    @Test
    fun `parses the asset without its trailing signature, ignoring unknown fields`() =
        runTest {
            val regions = fakeAssetDataSource.getRegions()

            assertEquals(listOf("vpn-regions.json"), reads)
            assertTrue(regions.isBundled)
            assertEquals(listOf("de"), regions.servers.map { it.key })
            assertEquals(listOf(8080), regions.info.udpPorts)
        }

    @Test
    fun `reads the asset only once`() =
        runTest {
            val first = fakeAssetDataSource.getRegions()
            val second = fakeAssetDataSource.getRegions()

            assertSame(first, second)
            assertEquals(1, reads.size)
        }

    @Test
    fun `fails on a malformed asset, and tries again on the next call`() =
        runTest {
            content = "not json"
            assertThrows<Exception> { fakeAssetDataSource.getRegions() }

            content = SIGNED_REGIONS
            assertEquals(listOf("de"), fakeAssetDataSource.getRegions().servers.map { it.key })
            assertEquals(2, reads.size)
        }

    private companion object {
        val SIGNED_REGIONS =
            """
            {"groups":{"ovpnudp":[{"name":"openvpn_udp","ports":[8080]}],"ikev2":[{"name":"ikev2","ports":[500]}]},
             "regions":[{"id":"de","name":"DE Berlin","country":"DE","auto_region":true,"dns":"de.example","port_forward":true,
              "geo":false,"offline":false,"unknown":1,"servers":{"meta":[{"ip":"1.1.1.1","cn":"de1"}]}}]}

            c2lnbmF0dXJl
            """.trimIndent()
    }
}