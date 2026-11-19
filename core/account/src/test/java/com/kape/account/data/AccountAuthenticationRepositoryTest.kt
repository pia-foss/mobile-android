package com.kape.account.data

import com.kape.account.domain.LoginResult
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy

internal class AccountAuthenticationRepositoryTest {
    private val api = FakeAuthenticationAccountApi()
    private val repository = AccountAuthenticationRepository(api)

    @Test
    fun `is logged in from cached tokens without loading them`() =
        runTest {
            api.apiToken = "api"
            api.vpnToken = "vpn"

            assertTrue(repository.isLoggedIn())
            assertEquals(emptyList<String>(), api.calls)
        }

    @Test
    fun `loads the stored tokens after a cold start`() =
        runTest {
            api.onLoadPersistedTokens = {
                api.apiToken = "api"
                api.vpnToken = "vpn"
            }

            assertTrue(repository.isLoggedIn())
            assertEquals(listOf("loadPersistedTokens"), api.calls)
        }

    @Test
    fun `is not logged in when a token is still missing after loading`() =
        runTest {
            api.onLoadPersistedTokens = { api.apiToken = "api" }

            assertFalse(repository.isLoggedIn())
        }

    @Test
    fun `is not logged in with an empty token`() =
        runTest {
            api.apiToken = ""
            api.vpnToken = "vpn"

            assertFalse(repository.isLoggedIn())
        }

    @Test
    fun `logs in with credentials and maps the account errors`() =
        runTest {
            api.errors = listOf(AccountRequestError(401, "unauthorized"))

            assertEquals(LoginResult.Failed, repository.loginWithCredentials("user", "secret"))
            assertEquals(listOf("loginWithCredentials(user, secret)"), api.calls)
        }

    @Test
    fun `requests a login link`() =
        runTest {
            assertEquals(LoginResult.Success, repository.requestLoginLink("me@example.com"))
            assertEquals(listOf("loginLink(me@example.com)"), api.calls)
        }

    @Test
    fun `logs in with a Google Play receipt`() =
        runTest {
            api.errors = listOf(AccountRequestError(402, "expired"))

            assertEquals(LoginResult.Expired, repository.loginWithReceipt("token", "yearly", "com.kape.vpn"))
            assertEquals(listOf("loginWithReceipt(google_play, token, yearly, com.kape.vpn)"), api.calls)
        }

    @Test
    fun `logs in with a login link token by migrating it`() =
        runTest {
            api.errors = listOf(AccountRequestError(503, "down"))

            assertEquals(LoginResult.ServiceUnavailable(503, "down"), repository.loginWithToken("abc"))
            assertEquals(listOf("migrateApiToken(abc)"), api.calls)
        }

    @Test
    fun `logout succeeds only without errors`() =
        runTest {
            assertTrue(repository.logout())

            api.errors = listOf(AccountRequestError(500, "error"))

            assertFalse(repository.logout())
        }
}

private class FakeAuthenticationAccountApi : AndroidAccountAPI by unsupported() {
    var apiToken: String? = null
    var vpnToken: String? = null
    var errors = emptyList<AccountRequestError>()
    var onLoadPersistedTokens: () -> Unit = {}
    val calls = mutableListOf<String>()

    override fun apiToken() = apiToken

    override fun vpnToken() = vpnToken

    override fun loadPersistedTokens(callback: () -> Unit) {
        calls += "loadPersistedTokens"
        onLoadPersistedTokens()
        callback()
    }

    override fun loginWithCredentials(
        username: String,
        password: String,
        callback: (List<AccountRequestError>) -> Unit,
    ) {
        calls += "loginWithCredentials($username, $password)"
        callback(errors)
    }

    override fun loginLink(
        email: String,
        callback: (List<AccountRequestError>) -> Unit,
    ) {
        calls += "loginLink($email)"
        callback(errors)
    }

    override fun loginWithReceipt(
        store: String,
        token: String,
        productId: String,
        applicationPackage: String,
        callback: (List<AccountRequestError>) -> Unit,
    ) {
        calls += "loginWithReceipt($store, $token, $productId, $applicationPackage)"
        callback(errors)
    }

    override fun migrateApiToken(
        apiToken: String,
        callback: (List<AccountRequestError>) -> Unit,
    ) {
        calls += "migrateApiToken($apiToken)"
        callback(errors)
    }

    override fun logout(callback: (List<AccountRequestError>) -> Unit) {
        callback(errors)
    }
}

private fun unsupported(): AndroidAccountAPI =
    Proxy.newProxyInstance(
        AndroidAccountAPI::class.java.classLoader,
        arrayOf(AndroidAccountAPI::class.java),
    ) { _, method, _ -> throw UnsupportedOperationException(method.name) } as AndroidAccountAPI