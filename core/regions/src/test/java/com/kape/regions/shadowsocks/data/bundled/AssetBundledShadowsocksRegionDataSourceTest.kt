package com.kape.regions.shadowsocks.data.bundled

import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.regions.common.data.AssetReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class AssetBundledShadowsocksRegionDataSourceTest {
    private val reads = mutableListOf<String>()
    private var content = SIGNED_SERVERS
    private val dataSource =
        AssetBundledShadowsocksRegionDataSource(
            AssetReader {
                reads += it
                content
            },
            Dispatchers.Unconfined,
        )

    @Test
    fun `parses the bundled asset`() =
        runTest {
            val assets = File("../../app/src/main/assets")
            val servers =
                AssetBundledShadowsocksRegionDataSource(AssetReader { File(assets, it).readText() }, Dispatchers.Unconfined)
                    .getServers()

            assertTrue(servers.isNotEmpty())
            assertTrue(servers.all { it.host.isNotEmpty() && it.port > 0 && it.region.isNotEmpty() })
        }

    @Test
    fun `parses the asset without its trailing signature, defaulting the country to world`() =
        runTest {
            assertEquals(
                listOf(ShadowsocksServer(iso = "world", region = "uk_manchester", host = "1.1.1.1", port = 443, key = "k", cipher = "c")),
                dataSource.getServers(),
            )
            assertEquals(listOf("shadowsocks-regions.json"), reads)
        }

    @Test
    fun `reads the asset only once`() =
        runTest {
            dataSource.getServers()
            dataSource.getServers()

            assertEquals(1, reads.size)
        }

    @Test
    fun `fails on a malformed asset, and tries again on the next call`() =
        runTest {
            content = """[{"region":"uk_manchester"}]"""
            assertThrows<Exception> { dataSource.getServers() }

            content = SIGNED_SERVERS
            assertEquals(1, dataSource.getServers().size)
            assertEquals(2, reads.size)
        }

    private companion object {
        val SIGNED_SERVERS =
            """
            [{"region":"uk_manchester","host":"1.1.1.1","port":443,"key":"k","cipher":"c","extra":true}]

            c2lnbmF0dXJl
            """.trimIndent()
    }
}