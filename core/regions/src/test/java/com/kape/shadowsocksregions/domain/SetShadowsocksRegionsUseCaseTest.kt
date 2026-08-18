package com.kape.shadowsocksregions.domain

import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.shadowsocksregions.data.ShadowsocksRegionRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SetShadowsocksRegionsUseCaseTest {
    private val shadowsocksRegionRepository: ShadowsocksRegionRepository = mockk(relaxed = true)
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs = mockk()
    private val readShadowsocksRegionsDetailsUseCase: ReadShadowsocksRegionsDetailsUseCase =
        mockk(relaxed = true)
    private val mockSetShadowsocksRegionsUseCase: SetShadowsocksRegionsUseCase =
        mockk(relaxed = true)

    private lateinit var getShadowsocksRegionsUseCase: GetShadowsocksRegionsUseCase
    private lateinit var setShadowsocksRegionsUseCase: SetShadowsocksRegionsUseCase

    @BeforeEach
    internal fun setUp() {
        getShadowsocksRegionsUseCase =
            GetShadowsocksRegionsUseCase(
                shadowsocksRegionRepository = shadowsocksRegionRepository,
                shadowsocksRegionPrefs = shadowsocksRegionPrefs,
                readShadowsocksRegionsDetailsUseCase = readShadowsocksRegionsDetailsUseCase,
                setShadowsocksRegionsUseCase = mockSetShadowsocksRegionsUseCase,
            )
        setShadowsocksRegionsUseCase =
            SetShadowsocksRegionsUseCase(
                shadowsocksRegionPrefs = shadowsocksRegionPrefs,
            )
    }

    @Test
    fun `set shadowsocks servers list`() =
        runTest {
            val expected = ShadowsocksServer(region = "", host = "", key = "", port = 0, cipher = "")
            coEvery { shadowsocksRegionPrefs.setSelectShadowsocksServer(any()) } returns Unit
            every { shadowsocksRegionPrefs.getSelectedShadowsocksServer() } returns flowOf(expected)
            every { shadowsocksRegionPrefs.shadowsocksServers } returns MutableStateFlow(listOf(expected))
            setShadowsocksRegionsUseCase.setSelectShadowsocksServer(expected)
            val actual = getShadowsocksRegionsUseCase.getSelectedShadowsocksServer().first()
            assertEquals(expected, actual)
        }

    @Test
    fun `set selected shadowsocks server`() =
        runTest {
            val expected: List<ShadowsocksServer> = listOf(mockk())
            coEvery { shadowsocksRegionPrefs.setShadowsocksServers(any()) } returns Unit
            every { shadowsocksRegionPrefs.shadowsocksServers } returns MutableStateFlow(expected)
            setShadowsocksRegionsUseCase.setShadowsocksServers(expected)
            val actual = getShadowsocksRegionsUseCase.getShadowsocksServers()
            assertEquals(expected, actual)
        }
}