package com.kape.vpn.automation

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StartAutomationIfEnabledUseCaseTest {
    private var starts = 0

    @Test
    fun `starts the service when automation is on`() =
        runTest {
            StartAutomationIfEnabledUseCase({ true }, { starts++ })()

            assertEquals(1, starts)
        }

    @Test
    fun `leaves it stopped when automation is off`() =
        runTest {
            StartAutomationIfEnabledUseCase({ false }, { starts++ })()

            assertEquals(0, starts)
        }
}