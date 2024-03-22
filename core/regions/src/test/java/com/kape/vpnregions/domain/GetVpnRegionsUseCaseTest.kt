package com.kape.vpnregions.domain

import app.cash.turbine.test
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.data.VpnRegionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.KoinTest
import java.util.stream.Stream
import kotlin.test.assertEquals

internal class GetVpnRegionsUseCaseTest : KoinTest {

    private val repo: VpnRegionRepository = mockk(relaxed = true)

    private lateinit var useCase: GetVpnRegionsUseCase

    @BeforeEach
    internal fun setUp() {
        useCase = GetVpnRegionsUseCase(repo)
    }

    @ParameterizedTest(name = "expected: {0}")
    @MethodSource("data")
    fun `fetch vpn regions`(expected: List<VpnServer>) = runTest {
        coEvery { repo.fetchVpnRegions(any()) } returns flow {
            emit(expected)
        }
        useCase.loadVpnServers("").test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    companion object {
        @JvmStatic
        fun data() = Stream.of(
            Arguments.of(emptyList<VpnServer>()),
            Arguments.of(
                listOf(
                    VpnServer(
                        name = "",
                        iso = "",
                        dns = "",
                        endpoints = emptyMap(),
                        key = "key",
                        isGeo = false,
                        isOffline = false,
                        allowsPortForwarding = false,
                        latency = null,
                        dedicatedIp = null,
                        dipToken = null,
                        latitude = null,
                        longitude = null,
                    ),
                ),
            ),
        )
    }
}