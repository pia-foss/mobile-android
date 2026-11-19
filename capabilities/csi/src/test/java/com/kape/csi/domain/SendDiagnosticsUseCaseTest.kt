package com.kape.csi.domain

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class SendDiagnosticsUseCaseTest {
    @Test
    fun `returns the report id`() =
        runTest {
            assertEquals("report-1", SendDiagnosticsUseCase { "report-1" }())
        }

    @Test
    fun `returns nothing when sending fails`() =
        runTest {
            assertNull(SendDiagnosticsUseCase { null }())
        }
}