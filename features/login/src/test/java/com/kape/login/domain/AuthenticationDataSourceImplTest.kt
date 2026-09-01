package com.kape.login.domain

import com.kape.contracts.AuthenticationDataSource
import com.kape.data.auth.ApiResult
import com.kape.login.BaseTest
import com.kape.login.data.AuthenticationDataSourceImpl
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

internal class AuthenticationDataSourceImplTest : BaseTest() {
    private val api: AndroidAccountAPI = mockk(relaxed = true)

    private lateinit var source: AuthenticationDataSource

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {}
        source = AuthenticationDataSourceImpl(api)
    }

    @ParameterizedTest(name = "api: {0}, expected: {1}")
    @MethodSource("accountApiResults")
    fun login(
        errorList: List<AccountRequestError>,
        expected: ApiResult,
    ) = runTest {
        coEvery { api.loginWithCredentials(any(), any(), any()) } answers {
            lastArg<(List<AccountRequestError>) -> Unit>().invoke(errorList)
        }
        val actual = source.login("user", "pass")
        assertEquals(expected, actual)
    }

    @ParameterizedTest(name = "api: {0}, expected: {1}")
    @MethodSource("accountApiResults")
    fun logout(
        errorList: List<AccountRequestError>,
        expected: ApiResult,
    ) = runTest {
        coEvery { api.logout(any()) } answers {
            lastArg<(List<AccountRequestError>) -> Unit>().invoke(errorList)
        }
        val actual = source.logout()
        assertEquals(expected, actual)
    }

    @ParameterizedTest(name = "api: {0}, expected: {1}")
    @MethodSource("accountApiResults")
    fun loginWithEmail(
        errorList: List<AccountRequestError>,
        expected: ApiResult,
    ) = runTest {
        coEvery { api.loginLink(any(), any()) } answers {
            lastArg<(List<AccountRequestError>) -> Unit>().invoke(errorList)
        }
        val actual = source.loginWithEmail("email")
        assertEquals(expected, actual)
    }

    @ParameterizedTest(name = "api: {0}, expected: {1}")
    @MethodSource("accountApiResults")
    fun loginWithReceipt(
        errorList: List<AccountRequestError>,
        expected: ApiResult,
    ) = runTest {
        coEvery { api.loginWithReceipt(any(), any(), any(), any(), any()) } answers {
            lastArg<(List<AccountRequestError>) -> Unit>().invoke(errorList)
        }
        val actual = source.loginWithReceipt("token", "product", "package")
        assertEquals(expected, actual)
    }

    @ParameterizedTest(name = "apiToken: {0}, vpnToken: {1}, expected: {2}")
    @MethodSource("tokens")
    fun isUserLoggedIn(
        apiToken: String?,
        vpnToken: String?,
        expected: Boolean,
    ) = runTest {
        every { api.apiToken() } returns apiToken
        every { api.vpnToken() } returns vpnToken
        assertEquals(expected, source.isUserLoggedIn())
    }

    @Test
    fun `isUserLoggedIn - logged in on first attempt - returns true without retrying`() =
        runTest {
            every { api.apiToken() } returns "apiToken"
            every { api.vpnToken() } returns "vpnToken"

            assertEquals(true, source.isUserLoggedIn())
        }

    @Test
    fun `isUserLoggedIn - logged in only after a transient failure - retries and returns true`() =
        runTest {
            every { api.apiToken() } returnsMany listOf(null, "apiToken")
            every { api.vpnToken() } returns "vpnToken"

            assertEquals(true, source.isUserLoggedIn())
        }

    @Test
    fun `isUserLoggedIn - never logged in - retries then returns false`() =
        runTest {
            every { api.apiToken() } returns null
            every { api.vpnToken() } returns null

            assertEquals(false, source.isUserLoggedIn())
        }

    @ParameterizedTest(name = "api: {0}, expected: {1}")
    @MethodSource("accountApiResults")
    fun migrateToken(
        errorList: List<AccountRequestError>,
        expected: ApiResult,
    ) = runTest {
        coEvery { api.migrateApiToken(any(), any()) } answers {
            lastArg<(List<AccountRequestError>) -> Unit>().invoke(errorList)
        }
        val actual = source.migrateToken("apiToken")
        assertEquals(expected, actual)
    }
}