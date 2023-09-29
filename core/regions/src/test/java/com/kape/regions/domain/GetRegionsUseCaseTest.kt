package com.kape.regions.domain

import app.cash.turbine.test
import com.kape.regions.RegionPrefs
import com.kape.regions.data.RegionRepository
import com.kape.utils.server.Server
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.KoinTest
import java.util.stream.Stream
import kotlin.test.assertEquals

internal class GetRegionsUseCaseTest : KoinTest {

    private val repo: RegionRepository = mockk(relaxed = true)
    private val prefs: RegionPrefs = mockk()

    private lateinit var useCase: GetRegionsUseCase

    @BeforeEach
    internal fun setUp() {
        useCase = GetRegionsUseCase(repo, prefs)
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

    @Test
    fun `select server`() = runTest {
        val expected = "selectedServer"
        every { prefs.selectServer(any()) } returns Unit
        every { prefs.getSelectedServerKey() } returns expected
        useCase.selectRegion(expected)
        val actual = useCase.getSelectedRegion()
        assertEquals(expected, actual)
    }

    @Test
    fun `get favorite servers if any`() = runTest {
        val expected = listOf("Germany", "UK")
        every { prefs.getFavoriteServers() } returns expected
        val actual = useCase.getFavoriteServers()
        assertEquals(expected, actual)
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
                        endpoints = emptyMap(),
                        key = "key",
                        isGeo = false,
                        isOffline = false,
                        isAllowsPF = false,
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