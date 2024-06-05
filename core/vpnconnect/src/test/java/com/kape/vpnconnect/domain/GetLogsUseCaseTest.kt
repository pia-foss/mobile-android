package com.kape.vpnconnect.domain

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GetLogsUseCaseTest {
    private val result = listOf("log1")
    private val connectionSource: ConnectionDataSource = mockk<ConnectionDataSource>().apply {
        coEvery { getDebugLogs() } returns flow { emit(result) }
    }

    private lateinit var useCase: GetLogsUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetLogsUseCase(connectionSource)
    }

    @Test
    fun `test getDebugLogs`() = runTest {
        useCase.getDebugLogs().test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(result, actual)
        }
    }
}