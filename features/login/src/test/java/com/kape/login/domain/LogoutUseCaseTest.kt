package com.kape.login.domain

import com.kape.contracts.AuthenticationDataSource
import com.kape.contracts.ConnectionManager
import com.kape.data.auth.ApiError
import com.kape.data.auth.ApiResult
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.login.BaseTest
import com.kape.login.domain.mobile.LogoutUseCaseImpl
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class LogoutUseCaseTest : BaseTest() {
    private val source = mockk<AuthenticationDataSource>()
    private val connectionManager = mockk<ConnectionManager>(relaxed = true)
    private val connectionPrefs = mockk<ConnectionPrefs>()
    private val settingsPrefs = mockk<SettingsPrefs>()
    private val logoutHandler = mockk<LogoutHandler>()

    private lateinit var useCase: LogoutUseCaseImpl

    @BeforeEach
    internal fun setUp() {
        useCase =
            LogoutUseCaseImpl(
                source,
                connectionPrefs,
                settingsPrefs,
                connectionManager,
                logoutHandler,
            )
    }

    @ParameterizedTest(name = "automationEnabled: {0}, result: {1}, expected: {2}")
    @MethodSource("data")
    fun logout(
        isAutomationEnabled: Boolean,
        result: ApiResult,
        expected: Boolean,
    ) = runTest {
        coEvery { source.logout() } returns result
        coEvery { connectionManager.disconnect() } returns Result.success(Unit)
        every { settingsPrefs.isAutomationEnabled.value } returns isAutomationEnabled
        coEvery { connectionPrefs.setDisconnectedByUser(any()) } returns Unit
        coEvery { connectionPrefs.clear() } returns Unit
        coEvery { settingsPrefs.clear() } returns Unit
        coEvery { logoutHandler.clearLocalStorage() } returns Unit

        val actual = useCase.logout()
        assertEquals(expected, actual)
    }

    companion object {
        @JvmStatic
        fun data() =
            Stream.of(
                Arguments.of(true, ApiResult.Success, true),
                Arguments.of(false, ApiResult.Success, true),
                Arguments.of(true, ApiResult.Error(ApiError.AuthFailed), false),
                Arguments.of(false, ApiResult.Error(ApiError.AccountExpired), false),
                Arguments.of(true, ApiResult.Error(ApiError.Throttled), false),
                Arguments.of(false, ApiResult.Error(ApiError.Unknown), false),
            )
    }
}