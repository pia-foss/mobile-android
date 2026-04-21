package com.kape.vpnconnect.domain

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GetLogsUseCaseTest {
    private val result = listOf("log1")
    private val connectionSource: ConnectionDataSource =
        mockk<ConnectionDataSource>().apply {
            coEvery { getDebugLogs() } returns result
        }

    private lateinit var useCase: GetLogsUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetLogsUseCase(connectionSource)
    }

    @Test
    fun `test getDebugLogs`() =
        runTest {
            val actual = useCase.getDebugLogs()
            assertEquals(result, actual)
        }
}