package com.kape.vpnregions.domain

import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.VpnRegionPrefs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest

class SetVpnRegionsUseCaseTest : KoinTest {
    private val prefs: VpnRegionPrefs = mockk(relaxed = true)

    private lateinit var useCase: SetVpnRegionsUseCase

    @BeforeEach
    fun setUp() {
        useCase = SetVpnRegionsUseCase(prefs)
    }

    @Test
    fun setVpnServers() = runTest {
        every { prefs.setVpnServers(any()) } returns Unit

        val expected = listOf<VpnServer>(mockk())
        useCase.setVpnServers(expected)
        verify { prefs.setVpnServers(expected) }
    }
}