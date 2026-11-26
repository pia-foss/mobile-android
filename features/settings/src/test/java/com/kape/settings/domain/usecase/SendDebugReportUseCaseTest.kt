package com.kape.settings.domain.usecase

import com.kape.contracts.DebugLogCleaner
import com.kape.csi.domain.SendDiagnosticsUseCase
import com.kape.localprefs.api.CsiStore
import com.kape.vpnconnect.domain.GetLogsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class SendDebugReportUseCaseTest {
    private val getLogs = mockk<GetLogsUseCase> { coEvery { getDebugLogs() } returns listOf("a", "b") }
    private val csiPrefs = mockk<CsiStore>(relaxed = true)
    private val cleaner = mockk<DebugLogCleaner>(relaxed = true)

    @Test
    fun `attaches the logs, sends, then deletes them`() =
        runTest {
            val send = mockk<SendDiagnosticsUseCase> { coEvery { this@mockk() } returns "ABC123" }

            assertEquals("ABC123", SendDebugReportUseCase(getLogs, csiPrefs, send, cleaner)())

            coVerifyOrder {
                csiPrefs.setProtocolDebugLogs("a\nb")
                send()
                csiPrefs.clearProtocolDebugLogs()
                cleaner.clearDebugLogs()
            }
        }

    @Test
    fun `keeps the logs when sending fails`() =
        runTest {
            val send = mockk<SendDiagnosticsUseCase> { coEvery { this@mockk() } returns null }

            assertNull(SendDebugReportUseCase(getLogs, csiPrefs, send, cleaner)())

            coVerify(exactly = 0) { cleaner.clearDebugLogs() }
        }
}