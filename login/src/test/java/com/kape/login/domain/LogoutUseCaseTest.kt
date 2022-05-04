package com.kape.login.domain

import app.cash.turbine.test
import com.kape.core.ApiError
import com.kape.core.ApiResult
import com.kape.login.BaseTest
import com.kape.login.data.AuthenticationDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@OptIn(ExperimentalCoroutinesApi::class)
internal class LogoutUseCaseTest: BaseTest() {
    private val source = mockk<AuthenticationDataSource>()

    private lateinit var useCase: LogoutUseCase

    @BeforeEach
    internal fun setUp() {
        useCase = LogoutUseCase(source)
    }


    @ParameterizedTest(name = "source: {0}, expected: {1}")
    @MethodSource("data")
    fun logout(result: ApiResult, expected: Boolean) = runTest {
        coEvery { source.logout() } returns flow {
            emit(result)
        }
        useCase.logout().test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    companion object {
        @JvmStatic
        fun data() = Stream.of(
            Arguments.of(ApiResult.Success, true),
            Arguments.of(ApiResult.Error(ApiError.AuthFailed), false),
            Arguments.of(ApiResult.Error(ApiError.AccountExpired), false),
            Arguments.of(ApiResult.Error(ApiError.Throttled), false),
            Arguments.of(ApiResult.Error(ApiError.Unknown), false)
        )
    }
}