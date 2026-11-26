package com.kape.regions.shadowsocks.domain.usecase

import com.kape.regions.shadowsocks.RecordingShadowsocksRegionRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RefreshShadowsocksServersUseCaseTest {
    @Test
    fun `refreshes the repository servers for the locale`() =
        runTest {
            val repository = RecordingShadowsocksRegionRepository()

            RefreshShadowsocksServersUseCase(repository)("fr")

            assertEquals(listOf("fr"), repository.refreshedLocales)
        }
}