package com.kape.vpnregions.data

import app.cash.turbine.test
import com.kape.dip.DipPrefs
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.domain.VpnRegionDataSource
import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.KoinTest
import java.util.stream.Stream

class VpnRegionRepositoryTest : KoinTest {
    private val source: VpnRegionDataSource = mockk()
    private val dipPrefs: DipPrefs = mockk()

    private lateinit var repository: VpnRegionRepository

    @BeforeEach
    fun setUp() {
        repository = VpnRegionRepository(source, dipPrefs)
    }

    @ParameterizedTest(name = "response: {0}, expected: {1}")
    @MethodSource("regions")
    fun fetchRegions(response: VpnRegionsResponse?, expected: List<VpnServer>) = runTest {
        coEvery { source.fetchVpnRegions(any()) } returns flow { emit(response) }
        every { dipPrefs.getDedicatedIps() } returns emptyList()

        repository.fetchVpnRegions("en").test {
            val actual = awaitItem()
            awaitComplete()
            Assertions.assertEquals(expected, actual)
        }
    }

    @ParameterizedTest(name = "response: {0}, expected: {1}")
    @MethodSource("latencies")
    fun fetchLatencies(
        regionsResponse: VpnRegionsResponse?,
        response: List<RegionLowerLatencyInformation>?,
        expected: List<VpnServer>,
    ) =
        runTest {
            coEvery { source.fetchVpnRegions(any()) } returns flow { emit(regionsResponse) }
            coEvery { source.pingRequests() } returns flow { emit(response) }
            every { dipPrefs.getDedicatedIps() } returns emptyList()

            repository.fetchVpnRegions("en").collect()
            repository.fetchLatencies().test {
                val actual = awaitItem()
                awaitComplete()
                Assertions.assertEquals(expected, actual)
            }
        }

    companion object {
        private val response = VpnRegionsResponse()
        private val server = VpnRegionsResponse.Region("id", "test", "android")
        private val anotherResponse = mockk<VpnRegionsResponse>().apply {
            every { regions } returns listOf(server)
            every { groups } returns emptyMap()
        }

        @JvmStatic
        fun regions() = Stream.of(
            Arguments.of(response, emptyList<VpnServer>()),
            Arguments.of(
                anotherResponse,
                listOf(
                    VpnServer(
                        "test",
                        "android",
                        null,
                        emptyMap(),
                        "id",
                        null,
                        null,
                        false,
                        false,
                        false,
                        null,
                        null,
                    ),
                ),
            ),
            Arguments.of(null, emptyList<VpnServer>()),
        )

        private val latencyInfo = RegionLowerLatencyInformation("id", "endpoint", 0)

        @JvmStatic
        fun latencies() = Stream.of(
            Arguments.of(
                response,
                emptyList<RegionLowerLatencyInformation>(),
                emptyList<VpnServer>(),
            ),
            Arguments.of(
                anotherResponse,
                listOf(latencyInfo),
                listOf(
                    VpnServer(
                        "test",
                        "android",
                        "0",
                        emptyMap(),
                        "id",
                        null,
                        null,
                        false,
                        false,
                        false,
                        null,
                        null,
                    ),
                ),
            ),
            Arguments.of(
                null,
                emptyList<RegionLowerLatencyInformation>(),
                emptyList<VpnServer>(),
            ),
        )
    }
}