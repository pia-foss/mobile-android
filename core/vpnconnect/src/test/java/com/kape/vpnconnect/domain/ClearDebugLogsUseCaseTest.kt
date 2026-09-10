package com.kape.vpnconnect.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ClearDebugLogsUseCaseTest {
    private val connectionSource: ConnectionDataSource =
        mockk<ConnectionDataSource>().apply {
            coEvery { clearDebugLogs() } returns Unit
        }

    private lateinit var useCase: ClearDebugLogsUseCase

    @BeforeEach
    fun setUp() {
        useCase = ClearDebugLogsUseCase(connectionSource)
    }

    @Test
    fun `clearDebugLogs - delegates to the connection data source`() =
        runTest {
            useCase.clearDebugLogs()

            coVerify { connectionSource.clearDebugLogs() }
        }
}