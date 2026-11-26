package com.kape.regions.shadowsocks.domain.usecase

import com.kape.regions.shadowsocks.RecordingShadowsocksRegionRepository
import com.kape.regions.shadowsocks.server
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ObserveShadowsocksServersUseCaseTest {
    @Test
    fun `observes the repository servers`() =
        runTest {
            val repository = RecordingShadowsocksRegionRepository().apply { servers.value = listOf(server("1.1.1.1")) }

            assertEquals(listOf(server("1.1.1.1")), ObserveShadowsocksServersUseCase(repository)().first())
        }
}