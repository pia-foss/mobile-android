package com.kape.login.domain

import com.kape.contracts.AuthenticationDataSource
import com.kape.data.auth.ApiError
import com.kape.data.auth.ApiResult
import com.kape.login.BaseTest
import com.kape.login.domain.mobile.LoginUseCase
import com.kape.login.utils.LoginState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

internal class LoginUseCaseTest : BaseTest() {
    private val source = mockk<AuthenticationDataSource>()

    private lateinit var useCase: LoginUseCase

    @BeforeEach
    internal fun setUp() {
        useCase = LoginUseCase(source)
    }

    @ParameterizedTest(name = "repo: {0}, expected: {1}")
    @MethodSource("useCaseLogin")
    fun login(
        result: ApiResult,
        expected: LoginState,
    ) = runTest {
        coEvery { source.login(any(), any()) } returns result
        val actual = useCase.login("user", "pass")
        assertEquals(expected, actual)
    }

    @ParameterizedTest(name = "repo: {0}, expected: {1}")
    @MethodSource("useCaseLogin")
    fun loginWithEmail(
        result: ApiResult,
        expected: LoginState,
    ) = runTest {
        coEvery { source.loginWithEmail(any()) } returns result
        val actual = useCase.loginWithEmail("email")
        assertEquals(expected, actual)
    }

    @ParameterizedTest(name = "repo: {0}, expected: {1}")
    @MethodSource("useCaseLogin")
    fun loginWithReceipt(
        result: ApiResult,
        expected: LoginState,
    ) = runTest {
        coEvery { source.loginWithReceipt(any(), any(), any()) } returns result
        val actual = useCase.loginWithReceipt("token", "product", "package")
        assertEquals(expected, actual)
    }

    companion object {
        @JvmStatic
        fun useCaseLogin() =
            Stream.of(
                Arguments.of(ApiResult.Success, LoginState.Successful),
                Arguments.of(ApiResult.Error(ApiError.AuthFailed), LoginState.Failed),
                Arguments.of(ApiResult.Error(ApiError.AccountExpired), LoginState.Expired),
                Arguments.of(ApiResult.Error(ApiError.Throttled), LoginState.Throttled),
                Arguments.of(ApiResult.Error(ApiError.Unknown), LoginState.Failed),
            )
    }
}