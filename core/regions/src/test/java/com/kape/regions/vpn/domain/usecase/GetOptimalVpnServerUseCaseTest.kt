package com.kape.regions.vpn.domain.usecase

import com.kape.regions.vpn.FakeVpnRegionRepository
import com.kape.regions.vpn.vpnServer
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class GetOptimalVpnServerUseCaseTest {
    @Test
    fun `waits for a non-empty server list, then picks the fastest auto region`() =
        runTest {
            val repository = FakeVpnRegionRepository()
            val optimal = async { GetOptimalVpnServerUseCase(repository)() }
            runCurrent()
            assertFalse(optimal.isCompleted)

            repository.servers.value =
                listOf(
                    vpnServer("slow", latency = "200"),
                    vpnServer("fast", latency = "20"),
                    vpnServer("manual", latency = "5", autoRegion = false),
                )

            assertEquals("fast", optimal.await().key)
        }
}