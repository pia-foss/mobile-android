package com.kape.profile.domain

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class DeleteAccountUseCaseTest {

    private val dataSource: ProfileDatasource = mockk()

    lateinit var useCase: DeleteAccountUseCase

    @BeforeEach
    fun setUp() {
        useCase = DeleteAccountUseCase(dataSource)
    }

    @ParameterizedTest(name = "result: {0}, expected: {1}")
    @MethodSource("data")
    fun deleteAccount(result: Boolean, expected: Boolean) = runTest {
        coEvery { dataSource.deleteAccount() } returns result
        val actual = useCase.deleteAccount()
        assertEquals(expected, actual)
    }

    companion object {
        @JvmStatic
        fun data() = Stream.of(
            Arguments.of(true, true),
            Arguments.of(false, false),
        )
    }
}