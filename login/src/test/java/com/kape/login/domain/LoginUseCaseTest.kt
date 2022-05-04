package com.kape.login.domain

import app.cash.turbine.test
import com.kape.login.BaseTest
import com.kape.login.utils.ApiError
import com.kape.login.utils.ApiResult
import com.kape.login.utils.LoginState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class LoginUseCaseTest : BaseTest() {

    private val repo = mockk<LoginRepository>()

    private lateinit var useCase: LoginUseCase

    @BeforeEach
    internal fun setUp() {
        useCase = LoginUseCase(repo)
    }

    @ParameterizedTest(name = "repo: {0}, expected: {1}")
    @MethodSource("useCaseLogin")
    fun login(result: ApiResult, expected: LoginState) = runTest {
        coEvery { repo.login(any(), any()) } returns flow {
            emit(result)
        }
        useCase.login("user", "pass").test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    companion object {
        @JvmStatic
        fun useCaseLogin() = Stream.of(
            Arguments.of(ApiResult.Success, LoginState.Successful),
            Arguments.of(ApiResult.Error(ApiError.AuthFailed), LoginState.Failed),
            Arguments.of(ApiResult.Error(ApiError.AccountExpired), LoginState.Expired),
            Arguments.of(ApiResult.Error(ApiError.Throttled), LoginState.Throttled),
            Arguments.of(ApiResult.Error(ApiError.Unknown), LoginState.Failed)
        )
    }
}