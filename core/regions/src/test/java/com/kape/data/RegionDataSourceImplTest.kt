package com.kape.data

import app.cash.turbine.test
import com.kape.shadowsocksregions.domain.ShadowsocksRegionDataSource
import com.kape.vpnregions.di.vpnRegionsModule
import com.kape.vpnregions.domain.VpnRegionDataSource
import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.RegionsAPI
import com.privateinternetaccess.regions.model.ShadowsocksRegionsResponse
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class RegionDataSourceImplTest : KoinTest {

    private val api: RegionsAPI = mockk(relaxed = true)
    private lateinit var vpnRegionDataSource: VpnRegionDataSource
    private lateinit var shadowsocksRegionDataSource: ShadowsocksRegionDataSource

    private val appModule = module {
        single { api }
    }

    @OptIn(DelicateCoroutinesApi::class)
    val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule, vpnRegionsModule(appModule))
        }
        vpnRegionDataSource = RegionDataSourceImpl(api)
        shadowsocksRegionDataSource = RegionDataSourceImpl(api)
    }

    @AfterEach
    internal fun tearDown() {
        Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun `fetch vpn regions success`() = runTest {
        val expected = VpnRegionsResponse()
        coEvery { api.fetchVpnRegions(any(), any()) } answers {
            lastArg<(VpnRegionsResponse?, Error?) -> Unit>().invoke(expected, null)
        }
        vpnRegionDataSource.fetchVpnRegions("").test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `fetch vpn regions error`() = runTest {
        val expected = null
        coEvery { api.fetchVpnRegions(any(), any()) } answers {
            lastArg<(VpnRegionsResponse?, Error?) -> Unit>().invoke(null, Error())
        }
        vpnRegionDataSource.fetchVpnRegions("").test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `fetch shadowsocks regions success`() = runTest {
        val expected: List<ShadowsocksRegionsResponse> = mockk()
        coEvery { api.fetchShadowsocksRegions(any(), any()) } answers {
            lastArg<(List<ShadowsocksRegionsResponse>, Error?) -> Unit>().invoke(expected, null)
        }
        shadowsocksRegionDataSource.fetchShadowsocksRegions("").test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `fetch shadowsocks regions error`() = runTest {
        val expected: List<ShadowsocksRegionsResponse> = emptyList()
        coEvery { api.fetchShadowsocksRegions(any(), any()) } answers {
            lastArg<(List<ShadowsocksRegionsResponse>, Error?) -> Unit>().invoke(emptyList(), Error())
        }
        shadowsocksRegionDataSource.fetchShadowsocksRegions("").test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `ping requests success`() = runTest {
        val expected = listOf<RegionLowerLatencyInformation>()
        coEvery { api.pingRequests(any()) } answers {
            lastArg<(List<RegionLowerLatencyInformation>, Error?) -> Unit>().invoke(expected, null)
        }
        vpnRegionDataSource.pingRequests().test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `ping requests error`() = runTest {
        val expected = emptyList<RegionLowerLatencyInformation>()
        coEvery { api.pingRequests(any()) } answers {
            lastArg<(List<RegionLowerLatencyInformation>, Error?) -> Unit>().invoke(expected, Error())
        }
        vpnRegionDataSource.pingRequests().test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }
}