package com.kape.region_selection.domain

import app.cash.turbine.test
import com.kape.region_selection.data.RegionRepository
import com.kape.region_selection.server.Server
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

@OptIn(ExperimentalCoroutinesApi::class)
internal class GetRegionsUseCaseTest : KoinTest {

    private val repo: RegionRepository = mockk(relaxed = true)

    private lateinit var useCase: GetRegionsUseCase

    @BeforeEach
    internal fun setUp() {
        useCase = GetRegionsUseCase(repo)
    }

    @ParameterizedTest(name = "expected: {0}")
    @MethodSource("data")
    fun `fetch regions`(expected: List<Server>) = runTest {
        coEvery { repo.fetchRegions(any()) } returns flow {
            emit(expected)
        }
        useCase.loadRegions("").test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    companion object {
        @JvmStatic
        fun data() = Stream.of(
            Arguments.of(emptyList<Server>()),
            Arguments.of(listOf(Server(name = "",
                iso = "",
                dns = "",
                endpoints = emptyMap(),
                key = "key",
                isGeo = false,
                isOffline = false,
                isAllowsPF = false,
                latency = null,
                dedicatedIp = null,
                dipToken = null,
                latitude = null,
                longitude = null
            )))
        )
    }
}