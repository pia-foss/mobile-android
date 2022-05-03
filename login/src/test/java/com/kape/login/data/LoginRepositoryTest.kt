package com.kape.login.data

import app.cash.turbine.test
import com.kape.login.BaseTest
import com.kape.login.utils.ApiError
import com.kape.login.utils.ApiResult
import com.kape.login.utils.LoginState
import com.kape.login.utils.Prefs
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
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@ExperimentalCoroutinesApi
internal class LoginRepositoryTest : BaseTest() {

    private val api: AndroidAccountAPI = mockk(relaxed = true)
    private val prefs: Prefs = mockk()

    private lateinit var repo: LoginRepository

    @BeforeEach
    internal fun setUp() {
        repo = LoginRepository(api, prefs)
    }

    @ParameterizedTest(name = "prefs: {0}")
    @MethodSource("booleans")
    fun isUserLoggedIn(expected: Boolean) = runTest {
        every { prefs.isUserLoggedIn() } returns expected
        val actual = repo.isUserLoggedIn()
        assertEquals(expected, actual)
    }

    @ParameterizedTest(name = "api: {0}, expected: {1}")
    @MethodSource("accountApiResults")
    fun login(errorList: List<AccountRequestError>, expected: ApiResult) =
        runTest {
            every { prefs.setUserLoggedIn(any()) } returns Unit
            coEvery { api.loginWithCredentials(any(), any(), any()) } answers {
                lastArg<(List<AccountRequestError>) -> Unit>().invoke(errorList)
            }

            repo.login("user", "pass").test {
                val actual = awaitItem()
                assertEquals(expected, actual)
            }
        }

    @ParameterizedTest(name = "api: {0}, expected: {1}")
    @MethodSource("accountApiResults")
    fun logout(errorList: List<AccountRequestError>, expected: ApiResult) =
        runTest {
            every { prefs.setUserLoggedIn(any()) } returns Unit
            coEvery { api.logout(any()) } answers {
                lastArg<(List<AccountRequestError>) -> Unit>().invoke(errorList)
            }

            repo.logout().test {
                val actual = awaitItem()
                kotlin.test.assertEquals(expected, actual)
            }
        }
}