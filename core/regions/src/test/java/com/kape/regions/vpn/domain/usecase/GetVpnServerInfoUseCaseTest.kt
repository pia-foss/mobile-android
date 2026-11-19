package com.kape.regions.vpn.domain.usecase

import com.kape.data.vpnserver.VpnServerInfo
import com.kape.regions.vpn.FakeVpnRegionRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GetVpnServerInfoUseCaseTest {
    @Test
    fun `returns the repository server info`() =
        runTest {
            val info = VpnServerInfo(listOf("de"), listOf(8080), listOf(443))
            val repository = FakeVpnRegionRepository().apply { serverInfo = info }

            assertEquals(info, GetVpnServerInfoUseCase(repository)())
        }
}