package com.kape.vpnregions.domain

import app.cash.turbine.test
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.data.VpnRegionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.KoinTest
import java.util.stream.Stream
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class UpdateLatencyUseCaseTest : KoinTest {
    private val repo: VpnRegionRepository = mockk(relaxed = true)

    private lateinit var useCase: UpdateLatencyUseCase

    @BeforeEach
    internal fun setUp() {
        useCase = UpdateLatencyUseCase(repo)
    }

    @ParameterizedTest(name = "expected: {0}")
    @MethodSource("data")
    fun `updateLatencies`(expected: List<VpnServer>) = runTest {
        coEvery { repo.fetchLatencies(false) } returns flow {
            emit(expected)
        }
        useCase.updateLatencies(false).test {
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
                        endpoints = emptyMap(),
                        key = "key",
                        isGeo = false,
                        isOffline = false,
                        allowsPortForwarding = false,
                        latency = "latency",
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