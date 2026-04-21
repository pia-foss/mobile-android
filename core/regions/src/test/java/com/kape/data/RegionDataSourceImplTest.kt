package com.kape.data

import com.kape.shadowsocksregions.domain.ShadowsocksRegionDataSource
import com.kape.vpnregions.domain.VpnRegionDataSource
import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.RegionsAPI
import com.privateinternetaccess.regions.model.ShadowsocksRegionsResponse
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals

internal class RegionDataSourceImplTest {
    private val api: RegionsAPI = mockk(relaxed = true)
    private lateinit var vpnRegionDataSource: VpnRegionDataSource
    private lateinit var shadowsocksRegionDataSource: ShadowsocksRegionDataSource

    private val appModule =
        module {
            single { api }
        }

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule)
        }
        vpnRegionDataSource = RegionDataSourceImpl(api)
        shadowsocksRegionDataSource = RegionDataSourceImpl(api)
    }

    @Test
    fun `fetch vpn regions success`() =
        runTest {
            val expected = VpnRegionsResponse()
            coEvery { api.fetchVpnRegions(any(), any()) } answers {
                lastArg<(VpnRegionsResponse?, Error?) -> Unit>().invoke(expected, null)
            }
            val actual = vpnRegionDataSource.fetchVpnRegions("")
            assertEquals(expected, actual)
        }

    @Test
    fun `fetch vpn regions error`() =
        runTest {
            coEvery { api.fetchVpnRegions(any(), any()) } answers {
                lastArg<(VpnRegionsResponse?, Error?) -> Unit>().invoke(null, Error())
            }
            val actual = vpnRegionDataSource.fetchVpnRegions("")
            assertEquals(null, actual)
        }

    @Test
    fun `fetch shadowsocks regions success`() =
        runTest {
            val expected: List<ShadowsocksRegionsResponse> = mockk()
            coEvery { api.fetchShadowsocksRegions(any(), any()) } answers {
                lastArg<(List<ShadowsocksRegionsResponse>, Error?) -> Unit>().invoke(expected, null)
            }
            val actual = shadowsocksRegionDataSource.fetchShadowsocksRegions("")
            assertEquals(expected, actual)
        }

    @Test
    fun `fetch shadowsocks regions error`() =
        runTest {
            coEvery { api.fetchShadowsocksRegions(any(), any()) } answers {
                lastArg<(List<ShadowsocksRegionsResponse>, Error?) -> Unit>().invoke(emptyList(), Error())
            }
            val actual = shadowsocksRegionDataSource.fetchShadowsocksRegions("")
            assertEquals(emptyList(), actual)
        }

    @Test
    fun `ping requests success`() =
        runTest {
            val expected = listOf<RegionLowerLatencyInformation>()
            coEvery { api.pingRequests(any()) } answers {
                lastArg<(List<RegionLowerLatencyInformation>, Error?) -> Unit>().invoke(expected, null)
            }
            val actual = vpnRegionDataSource.pingRequests()
            assertEquals(expected, actual)
        }

    @Test
    fun `ping requests error`() =
        runTest {
            coEvery { api.pingRequests(any()) } answers {
                lastArg<(List<RegionLowerLatencyInformation>, Error?) -> Unit>().invoke(emptyList(), Error())
            }
            val actual = vpnRegionDataSource.pingRequests()
            assertEquals(null, actual)
        }
}