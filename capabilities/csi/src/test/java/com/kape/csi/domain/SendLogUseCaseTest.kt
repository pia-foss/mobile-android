package com.kape.csi.domain

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SendLogUseCaseTest {

    private val dataSource: CsiDataSource = mockk()
    private lateinit var useCase: SendLogUseCase

    @BeforeEach
    fun setUp() {
        useCase = SendLogUseCase(dataSource)
    }

    @Test
    fun `send - success`() = runTest {
        val expected = "OK"
        every { dataSource.send() } returns flow { emit(expected) }
        useCase.sendLog().test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `send - failure`() = runTest {
        val expected = ""
        every { dataSource.send() } returns flow { emit(expected) }
        useCase.sendLog().test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
            assertTrue(actual.isEmpty())
        }
    }
}