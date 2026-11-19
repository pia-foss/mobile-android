package com.kape.regions.shadowsocks.data.remote

import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.regions.common.FakeRegionsApi
import com.privateinternetaccess.regions.model.ShadowsocksRegionsResponse
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ShadowsocksRegionRemoteDataSourceTest {
    private val api = FakeRegionsApi()
    private val dataSource = RegionsApiShadowsocksRemoteDataSource(api)

    @Test
    fun `maps every fetched region to a server`() =
        runTest {
            api.shadowsocksRegions =
                listOf(
                    ShadowsocksRegionsResponse("GB", "uk_manchester", "1.1.1.1", 443, "key", "aes-128-gcm"),
                    ShadowsocksRegionsResponse("CA", "ca_vancouver", "2.2.2.2", 8443, "key2", "chacha20"),
                )

            assertEquals(
                listOf(
                    ShadowsocksServer(
                        iso = "GB",
                        region = "uk_manchester",
                        host = "1.1.1.1",
                        port = 443,
                        key = "key",
                        cipher = "aes-128-gcm",
                    ),
                    ShadowsocksServer(
                        iso = "CA",
                        region = "ca_vancouver",
                        host = "2.2.2.2",
                        port = 8443,
                        key = "key2",
                        cipher = "chacha20",
                    ),
                ),
                dataSource.fetchServers("en"),
            )
            assertEquals(listOf("en"), api.requestedLocales)
        }

    @Test
    fun `returns no servers when the request fails`() =
        runTest {
            api.shadowsocksRegions = listOf(ShadowsocksRegionsResponse("GB", "uk_manchester", "1.1.1.1", 443, "key", "aes-128-gcm"))
            api.error = Error("offline")

            assertEquals(emptyList<ShadowsocksServer>(), dataSource.fetchServers("en"))
        }
}