package com.kape.login.domain

import app.cash.turbine.test
import com.kape.login.BaseTest
import com.kape.login.data.AuthenticationDataSourceImpl
import com.kape.login.di.loginModule
import com.kape.login.domain.mobile.AuthenticationDataSource
import com.kape.utils.ApiResult
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@ExperimentalCoroutinesApi
internal class AuthenticationDataSourceImplTest : BaseTest() {

    private val api: AndroidAccountAPI = mockk(relaxed = true)

    private lateinit var source: AuthenticationDataSource

    private val appModule = module {
        single { api }
    }

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule, loginModule(appModule))
        }
        source = AuthenticationDataSourceImpl(api)
    }

    @ParameterizedTest(name = "api: {0}, expected: {1}")
    @MethodSource("accountApiResults")
    fun login(errorList: List<AccountRequestError>, expected: ApiResult) =
        runTest {
            coEvery { api.loginWithCredentials(any(), any(), any()) } answers {
                lastArg<(List<AccountRequestError>) -> Unit>().invoke(errorList)
            }

            source.login("user", "pass").test {
                val actual = awaitItem()
                assertEquals(expected, actual)
            }
        }

    @ParameterizedTest(name = "api: {0}, expected: {1}")
    @MethodSource("accountApiResults")
    fun logout(errorList: List<AccountRequestError>, expected: ApiResult) =
        runTest {
            coEvery { api.logout(any()) } answers {
                lastArg<(List<AccountRequestError>) -> Unit>().invoke(errorList)
            }

            source.logout().test {
                val actual = awaitItem()
                kotlin.test.assertEquals(expected, actual)
            }
        }

    @ParameterizedTest(name = "api: {0}, expected: {1}")
    @MethodSource("accountApiResults")
    fun loginWithEmail(errorList: List<AccountRequestError>, expected: ApiResult) =
        runTest {
            coEvery { api.loginLink(any(), any()) } answers {
                lastArg<(List<AccountRequestError>) -> Unit>().invoke(errorList)
            }

            source.loginWithEmail("email").test {
                val actual = awaitItem()
                assertEquals(expected, actual)
            }
        }

    @ParameterizedTest(name = "api: {0}, expected: {1}")
    @MethodSource("accountApiResults")
    fun loginWithReceipt(errorList: List<AccountRequestError>, expected: ApiResult) =
        runTest {
            coEvery { api.loginWithReceipt(any(), any(), any(), any(), any()) } answers {
                lastArg<(List<AccountRequestError>) -> Unit>().invoke(errorList)
            }

            source.loginWithReceipt("token", "product", "package").test {
                val actual = awaitItem()
                assertEquals(expected, actual)
            }
        }

    @ParameterizedTest(name = "apiToken: {0}, vpnToken: {1}, expected: {2}")
    @MethodSource("tokens")
    fun isUserLoggedIn(apiToken: String?, vpnToken: String?, expected: Boolean) = runTest {
        every { api.apiToken() } returns apiToken
        every { api.vpnToken() } returns vpnToken

        assertEquals(expected, source.isUserLoggedIn())
    }

    @ParameterizedTest(name = "api: {0}, expected: {1}")
    @MethodSource("accountApiResults")
    fun migrateToken(errorList: List<AccountRequestError>, expected: ApiResult) = runTest {
        coEvery { api.migrateApiToken(any(), any()) } answers {
            lastArg<(List<AccountRequestError>) -> Unit>().invoke(errorList)
        }

        source.migrateToken("apiToken").test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }
}