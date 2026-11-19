package com.kape.regions.vpn.domain.usecase

import com.kape.regions.vpn.FakeVpnRegionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class ObserveIsDefaultVpnServerListUseCaseTest {
    @Test
    fun `observes whether the repository still serves the default list`() =
        runTest {
            val repository = FakeVpnRegionRepository().apply { isDefaultList.value = false }

            assertFalse(ObserveIsDefaultVpnServerListUseCase(repository)().first())
        }
}