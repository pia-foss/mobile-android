package com.kape.csi.domain

import io.mockk.coEvery
import io.mockk.mockk
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
    fun `send - success`() =
        runTest {
            val expected = "OK"
            coEvery { dataSource.send() } returns expected
            val actual = useCase.sendLog()
            assertEquals(expected, actual)
        }

    @Test
    fun `send - failure`() =
        runTest {
            val expected = ""
            coEvery { dataSource.send() } returns expected
            val actual = useCase.sendLog()
            assertEquals(expected, actual)
            assertTrue(actual.isEmpty())
        }
}