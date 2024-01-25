package com.kape.signup.domain

import com.kape.signup.ConsentPrefs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.KoinTest
import java.util.stream.Stream

class ConsentUseCaseTest : KoinTest {
    private val prefs: ConsentPrefs = mockk()

    private lateinit var useCase: ConsentUseCase

    @BeforeEach
    fun setUp() {
        useCase = ConsentUseCase(prefs)
    }

    @ParameterizedTest(name = "result: {0}, expected: {1}")
    @MethodSource("booleans")
    fun testConsent(result: Boolean, expected: Boolean) = runTest {
        every { prefs.setAllowSharing(any()) } returns Unit
        every { prefs.getAllowSharing() } returns expected

        useCase.setConsent(result)
        assertEquals(expected, useCase.getConsent())
    }

    companion object {
        @JvmStatic
        fun booleans() =
            Stream.of(
                Arguments.of(true, true),
                Arguments.of(false, false),
            )
    }
}