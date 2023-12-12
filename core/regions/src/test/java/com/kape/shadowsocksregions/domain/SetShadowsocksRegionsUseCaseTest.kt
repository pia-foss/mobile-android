package com.kape.shadowsocksregions.domain

import com.kape.shadowsocksregions.ShadowsocksRegionPrefs
import com.kape.shadowsocksregions.data.ShadowsocksRegionRepository
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest
import kotlin.test.assertEquals

internal class SetShadowsocksRegionsUseCaseTest : KoinTest {

    private val shadowsocksRegionRepository: ShadowsocksRegionRepository = mockk(relaxed = true)
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs = mockk()

    private lateinit var getShadowsocksRegionsUseCase: GetShadowsocksRegionsUseCase
    private lateinit var setShadowsocksRegionsUseCase: SetShadowsocksRegionsUseCase

    @BeforeEach
    internal fun setUp() {
        getShadowsocksRegionsUseCase = GetShadowsocksRegionsUseCase(
            shadowsocksRegionRepository = shadowsocksRegionRepository,
            shadowsocksRegionPrefs = shadowsocksRegionPrefs,
        )
        setShadowsocksRegionsUseCase = SetShadowsocksRegionsUseCase(
            shadowsocksRegionPrefs = shadowsocksRegionPrefs,
        )
    }

    @Test
    fun `set shadowsocks servers list`() = runTest {
        val expected = "setShadowsocksServerList"
        every { shadowsocksRegionPrefs.setSelectShadowsocksServer(any()) } returns Unit
        every { shadowsocksRegionPrefs.getSelectedShadowsocksServerKey() } returns expected
        setShadowsocksRegionsUseCase.setSelectShadowsocksServer(expected)
        val actual = getShadowsocksRegionsUseCase.getSelectedShadowsocksServerKey()
        assertEquals(expected, actual)
    }

    @Test
    fun `set selected shadowsocks server`() = runTest {
        val expected: List<ShadowsocksServer> = listOf(mockk())
        every { shadowsocksRegionPrefs.setShadowsocksServers(any()) } returns Unit
        every { shadowsocksRegionPrefs.getShadowsocksServers() } returns expected
        setShadowsocksRegionsUseCase.setShadowsocksServers(expected)
        val actual = getShadowsocksRegionsUseCase.getShadowsocksServers()
        assertEquals(expected, actual)
    }

    @Test
    fun `get selected shadowsocks server if any`() = runTest {
        val expected = "setSelectedShadowsocksServer"
        every { shadowsocksRegionPrefs.getSelectedShadowsocksServerKey() } returns expected
        val actual = getShadowsocksRegionsUseCase.getSelectedShadowsocksServerKey()
        assertEquals(expected, actual)
    }
}