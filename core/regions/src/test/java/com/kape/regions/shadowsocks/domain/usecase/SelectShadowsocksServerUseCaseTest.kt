package com.kape.regions.shadowsocks.domain.usecase

import com.kape.regions.shadowsocks.RecordingShadowsocksRegionRepository
import com.kape.regions.shadowsocks.server
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SelectShadowsocksServerUseCaseTest {
    @Test
    fun `selects the server in the repository`() =
        runTest {
            val repository = RecordingShadowsocksRegionRepository()

            SelectShadowsocksServerUseCase(repository)(server("1.1.1.1"))

            assertEquals(server("1.1.1.1"), repository.selected.value)
        }
}