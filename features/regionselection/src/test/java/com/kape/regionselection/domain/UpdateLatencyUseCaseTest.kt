package com.kape.regionselection.domain

import app.cash.turbine.test
import com.kape.regionselection.data.RegionRepository
import com.kape.utils.server.Server
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
    private val repo: RegionRepository = mockk(relaxed = true)

    private lateinit var useCase: UpdateLatencyUseCase

    @BeforeEach
    internal fun setUp() {
        useCase = UpdateLatencyUseCase(repo)
    }

    @ParameterizedTest(name = "expected: {0}")
    @MethodSource("data")
    fun `updateLatencies`(expected: List<Server>) = runTest {
        coEvery { repo.fetchLatencies() } returns flow {
            emit(expected)
        }
        useCase.updateLatencies().test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    companion object {
        @JvmStatic
        fun data() = Stream.of(
            Arguments.of(emptyList<Server>()),
            Arguments.of(
                listOf(
                    Server(
                        name = "",
                        iso = "",
                        dns = "",
                        endpoints = emptyMap(),
                        key = "key",
                        isGeo = false,
                        isOffline = false,
                        isAllowsPF = false,
                        latency = "latency",
                        dedicatedIp = null,
                        dipToken = null,
                        latitude = null,
                        longitude = null
                    )
                )
            )
        )
    }
}