package com.kape.regions.vpn.domain.usecase

import com.kape.regions.vpn.FakeVpnRegionRepository
import com.kape.regions.vpn.vpnServer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ObserveVpnServersUseCaseTest {
    @Test
    fun `observes the repository servers`() =
        runTest {
            val repository = FakeVpnRegionRepository(listOf(vpnServer("de"), vpnServer("fr")))

            assertEquals(listOf("de", "fr"), ObserveVpnServersUseCase(repository)().first().map { it.key })
        }
}