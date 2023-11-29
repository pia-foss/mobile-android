package com.kape.vpnregions.data

import app.cash.turbine.test
import com.kape.vpnregions.di.regionsModule
import com.kape.vpnregions.domain.VpnRegionDataSource
import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.RegionsAPI
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
internal class VpnRegionDataSourceImplTest : KoinTest {

    private val api: RegionsAPI = mockk(relaxed = true)
    private lateinit var source: VpnRegionDataSource

    private val appModule = module {
        single { api }
    }

    @OptIn(DelicateCoroutinesApi::class)
    val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule, regionsModule(appModule))
        }
        source = VpnRegionDataSourceImpl(api)
    }

    @AfterEach
    internal fun tearDown() {
        Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun `fetch regions success`() = runTest {
        val expected = VpnRegionsResponse()
        coEvery { api.fetchVpnRegions(any(), any()) } answers {
            lastArg<(VpnRegionsResponse?, List<Error>) -> Unit>().invoke(expected, emptyList())
        }
        source.fetchRegions("").test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `fetch regions error`() = runTest {
        val expected = null
        coEvery { api.fetchVpnRegions(any(), any()) } answers {
            lastArg<(VpnRegionsResponse?, List<Error>) -> Unit>().invoke(null, listOf(Error()))
        }
        source.fetchRegions("").test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `ping requests success`() = runTest {
        val expected = listOf<RegionLowerLatencyInformation>()
        coEvery { api.pingRequests(any()) } answers {
            lastArg<(List<RegionLowerLatencyInformation>, List<Error>) -> Unit>().invoke(expected, emptyList())
        }
        source.pingRequests().test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `ping requests error`() = runTest {
        val expected = emptyList<RegionLowerLatencyInformation>()
        coEvery { api.pingRequests(any()) } answers {
            lastArg<(List<RegionLowerLatencyInformation>, List<Error>) -> Unit>().invoke(expected, listOf(Error()))
        }
        source.pingRequests().test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }
}