package com.kape.vpnregions.data

import com.kape.contracts.ConnectionConfigurationUseCase
import com.kape.contracts.ConnectionInfoProvider
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.DipPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import com.kape.vpnregions.domain.VpnRegionDataSource
import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class VpnRegionRepositoryTest {
    private val source: VpnRegionDataSource = mockk()
    private val dipPrefs: DipPrefs = mockk()
    private val connectionPrefs: ConnectionPrefs = mockk(relaxed = true)
    private val settingsPrefs: SettingsPrefs = mockk(relaxed = true)
    private val connectionInfoProvider: ConnectionInfoProvider = mockk(relaxed = true)
    private val connectionConfigurationUseCase: ConnectionConfigurationUseCase = mockk(relaxed = true)
    private val readVpnRegionsDetailsUseCase: ReadVpnRegionsDetailsUseCase = mockk()

    private lateinit var repository: VpnRegionRepository

    @BeforeEach
    fun setUp() {
        // Matches the pre-fix behaviour (empty fallback) for every test that isn't specifically
        // exercising the cold-start-falls-back-to-bundled-assets path below.
        every { readVpnRegionsDetailsUseCase.readVpnRegionsDetailsFromAssetsFolder() } returns emptyList()

        repository =
            VpnRegionRepository(
                source,
                dipPrefs,
                connectionPrefs,
                settingsPrefs,
                connectionInfoProvider,
                connectionConfigurationUseCase,
                readVpnRegionsDetailsUseCase,
            )
    }

    @ParameterizedTest(name = "response: {0}, expected: {1}")
    @MethodSource("regions")
    fun fetchRegions(
        response: VpnRegionsResponse?,
        expected: List<VpnServer>,
    ) = runTest {
        coEvery { source.fetchVpnRegions(any()) } returns response
        every { dipPrefs.dedicatedIps } returns MutableStateFlow(emptyList())

        val actual = repository.fetchVpnRegions("en")
        Assertions.assertEquals(expected, actual)
    }

    @ParameterizedTest(name = "response: {0}, expected: {1}")
    @MethodSource("latencies")
    fun fetchLatencies(
        regionsResponse: VpnRegionsResponse?,
        response: List<RegionLowerLatencyInformation>?,
        expected: List<VpnServer>,
    ) = runTest {
        coEvery { source.fetchVpnRegions(any()) } returns regionsResponse
        coEvery { source.pingRequests() } returns response
        every { dipPrefs.dedicatedIps } returns MutableStateFlow(emptyList())

        repository.fetchVpnRegions("en")
        val actual = repository.fetchLatencies(false)
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun fetchCachedListWhenRefreshFails() =
        runTest {
            every { dipPrefs.dedicatedIps } returns MutableStateFlow(emptyList())
            coEvery { source.fetchVpnRegions(any()) } returns anotherResponse

            val cachedList = repository.fetchVpnRegions("en")

            VpnRegionRepository::class.java.getDeclaredField("lastUpdate").apply {
                isAccessible = true
                setLong(repository, 0L)
            }
            coEvery { source.fetchVpnRegions(any()) } returns null

            val actual = repository.fetchVpnRegions("en")

            Assertions.assertEquals(cachedList, actual)
        }

    @Test
    fun `test fetchVpnRegions falls back to bundled asset data on a cold start network failure`() =
        runTest {
            // Nothing has ever been fetched successfully yet (serverMap starts empty) - this is
            // the exact scenario of a fresh install with a flaky/unreachable network.
            every { dipPrefs.dedicatedIps } returns MutableStateFlow(emptyList())
            coEvery { source.fetchVpnRegions(any()) } returns null
            every { readVpnRegionsDetailsUseCase.readVpnRegionsDetailsFromAssetsFolder() } returns
                listOf(bundledServer)

            val actual = repository.fetchVpnRegions("en")

            Assertions.assertEquals(listOf(bundledServer), actual)
        }

    @Test
    fun `test fetchVpnRegions only reads the bundled assets once, not on every failed retry`() =
        runTest {
            every { dipPrefs.dedicatedIps } returns MutableStateFlow(emptyList())
            coEvery { source.fetchVpnRegions(any()) } returns null
            every { readVpnRegionsDetailsUseCase.readVpnRegionsDetailsFromAssetsFolder() } returns
                listOf(bundledServer)

            repository.fetchVpnRegions("en")
            repository.fetchVpnRegions("en")

            verify(exactly = 1) { readVpnRegionsDetailsUseCase.readVpnRegionsDetailsFromAssetsFolder() }
        }

    companion object {
        private val bundledServer =
            VpnServer(
                "Bundled",
                "bundled",
                "",
                null,
                emptyMap(),
                "bundled-id",
                null,
                null,
                false,
                false,
                false,
                false,
                null,
                null,
            )
        private val response = VpnRegionsResponse()
        private val server = VpnRegionsResponse.Region("id", "test", "android")
        private val anotherResponse =
            mockk<VpnRegionsResponse>().apply {
                every { regions } returns listOf(server)
                every { groups } returns emptyMap()
            }

        @JvmStatic
        fun regions() =
            Stream.of(
                Arguments.of(response, emptyList<VpnServer>()),
                Arguments.of(
                    anotherResponse,
                    listOf(
                        VpnServer(
                            "test",
                            "android",
                            "",
                            null,
                            emptyMap(),
                            "id",
                            null,
                            null,
                            false,
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

        private val latencyInfo = RegionLowerLatencyInformation("id", 0)

        @JvmStatic
        fun latencies() =
            Stream.of(
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
                            "",
                            "0",
                            emptyMap(),
                            "id",
                            null,
                            null,
                            false,
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