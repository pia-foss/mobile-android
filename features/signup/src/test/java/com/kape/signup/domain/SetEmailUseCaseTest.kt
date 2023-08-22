package com.kape.signup.domain

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

internal class SetEmailUseCaseTest {

    private val source: EmailDataSource = mockk(relaxed = true)

    private lateinit var useCase: SetEmailUseCase

    @BeforeEach
    fun setUp() {
        useCase = SetEmailUseCase(source)
    }

    @ParameterizedTest(name = "expected: {0}")
    @MethodSource("booleans")
    fun `test set email`(expected: Boolean) = runTest {
        coEvery { source.setEmail(any()) } returns flow { emit(expected) }

        useCase.setEmail("email").test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    companion object {
        @JvmStatic
        fun booleans() =
            Stream.of(
                Arguments.of(true),
                Arguments.of(false),
            )
    }
}