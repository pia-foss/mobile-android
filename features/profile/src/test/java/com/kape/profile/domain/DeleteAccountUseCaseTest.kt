package com.kape.profile.domain

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.KoinTest
import java.util.stream.Stream
import kotlin.test.assertEquals

class DeleteAccountUseCaseTest : KoinTest {

    private val dataSource: ProfileDatasource = mockk()

    lateinit var useCase: DeleteAccountUseCase

    @BeforeEach
    fun setUp() {
        useCase = DeleteAccountUseCase(dataSource)
    }

    @ParameterizedTest(name = "result: {0}, expected: {1}")
    @MethodSource("data")
    fun deleteAccount(result: Boolean, expected: Boolean) = runTest {
        every { dataSource.deleteAccount() } returns flow { emit(result) }

        useCase.deleteAccount().test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    companion object {
        @JvmStatic
        fun data() = Stream.of(
            Arguments.of(true, true),
            Arguments.of(false, false),
        )
    }
}