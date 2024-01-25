package com.kape.profile.domain

import app.cash.turbine.test
import com.kape.profile.data.models.Profile
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

class GetProfileUseCaseTest : KoinTest {

    private val dataSource: ProfileDatasource = mockk()

    lateinit var useCase: GetProfileUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetProfileUseCase(dataSource)
    }

    @ParameterizedTest(name = "automationEnabled: {0}, connectionActive: {1}, result: {2}, expected: {3}")
    @MethodSource("data")
    fun logout(
        result: Profile?,
        expected: Profile?,
    ) = runTest {
        every { dataSource.accountDetails() } returns flow { emit(result) }

        useCase.getProfile().test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    companion object {
        private val result = Profile(username = "username", subscription = mockk())

        @JvmStatic
        fun data() = Stream.of(
            Arguments.of(null, null),
            Arguments.of(result, result),
        )
    }
}