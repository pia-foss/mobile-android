package com.kape.shadowsocksregions.domain

import app.cash.turbine.test
import com.kape.shadowsocksregions.ShadowsocksRegionPrefs
import com.kape.shadowsocksregions.data.ShadowsocksRegionRepository
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.KoinTest
import java.util.stream.Stream
import kotlin.test.assertEquals

internal class GetShadowsocksRegionsUseCaseTest : KoinTest {

    private val shadowsocksRegionRepository: ShadowsocksRegionRepository = mockk(relaxed = true)
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs = mockk()
    private val readShadowsocksRegionsDetailsUseCase: ReadShadowsocksRegionsDetailsUseCase = mockk(relaxed = true)
    private val setShadowsocksRegionsUseCase: SetShadowsocksRegionsUseCase = mockk(relaxed = true)

    private lateinit var getShadowsocksRegionsUseCase: GetShadowsocksRegionsUseCase

    @BeforeEach
    internal fun setUp() {
        getShadowsocksRegionsUseCase = GetShadowsocksRegionsUseCase(
            shadowsocksRegionRepository,
            shadowsocksRegionPrefs,
            readShadowsocksRegionsDetailsUseCase,
            setShadowsocksRegionsUseCase,
        )
    }

    @ParameterizedTest(name = "expected: {0}")
    @MethodSource("shadowsocksListData")
    fun `fetch shadowsocks regions`(expected: List<ShadowsocksServer>) = runTest {
        coEvery { shadowsocksRegionRepository.fetchShadowsocksServers(any()) } returns flow {
            emit(expected)
        }
        getShadowsocksRegionsUseCase.fetchShadowsocksServers("").test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    @ParameterizedTest(name = "expected: {0}")
    @MethodSource("shadowsocksListData")
    fun `get selected shadowsocks region set and return a default when none has been selected by the user`(
        expected: List<ShadowsocksServer>,
    ) = runTest {
        // given
        every { shadowsocksRegionPrefs.getShadowsocksServers() } returns expected
        every { shadowsocksRegionPrefs.getSelectedShadowsocksServer() } returns null

        // when
        getShadowsocksRegionsUseCase.getSelectedShadowsocksServer()

        // then
        verify {
            setShadowsocksRegionsUseCase.setSelectShadowsocksServer(expected.first())
        }
    }

    companion object {
        @JvmStatic
        fun shadowsocksListData(): Stream<Arguments> = Stream.of(
            Arguments.of(
                listOf(
                    ShadowsocksServer(
                        region = "region1",
                        host = "host",
                        port = 8080,
                        key = "key",
                        cipher = "cipher",
                    ),
                    ShadowsocksServer(
                        region = "region2",
                        host = "host",
                        port = 8080,
                        key = "key",
                        cipher = "cipher",
                    ),
                ),
            ),
        )
    }
}