package com.kape.login.domain.usecase

import com.kape.account.domain.LoginResult
import com.kape.account.domain.usecase.GetPostLoginDestinationUseCase
import com.kape.account.domain.usecase.LoginWithReceiptUseCase
import com.kape.data.Connection
import com.kape.data.LoginWithCredentials
import com.kape.data.NotificationPermission
import com.kape.data.VpnPermission
import com.kape.login.FakeAuthentication
import com.kape.login.FakePermissions
import com.kape.login.Recorder
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class LoginUseCasesTest {
    private val recorder = Recorder()
    private val authentication = FakeAuthentication(recorder)
    private val permissions = FakePermissions()
    private val postLogin = GetPostLoginDestinationUseCase(permissions)

    @Test
    fun `rejects empty credentials without calling the server`() =
        runTest {
            val login = LoginWithCredentialsUseCase(authentication)

            assertEquals(LoginResult.InvalidInput, login("", "secret"))
            assertEquals(LoginResult.InvalidInput, login("user", ""))
            assertEquals(emptyList<String>(), recorder.events)
        }

    @Test
    fun `sends valid credentials to the server and returns its result`() =
        runTest {
            authentication.result = LoginResult.Expired

            assertEquals(LoginResult.Expired, LoginWithCredentialsUseCase(authentication)("user", "secret"))
            assertEquals(listOf("login(user)"), recorder.events)
        }

    @Test
    fun `requests a login link for a non-empty email`() =
        runTest {
            assertEquals(LoginResult.Success, RequestLoginLinkUseCase(authentication)("me@example.com"))
            assertEquals(listOf("link(me@example.com)"), recorder.events)
        }

    @Test
    fun `logs in with the store receipt`() =
        runTest {
            authentication.result = LoginResult.Throttled

            assertEquals(LoginResult.Throttled, LoginWithReceiptUseCase(authentication)("token", "yearly", "com.kape.vpn"))
            assertEquals(listOf("receipt(token, yearly, com.kape.vpn)"), recorder.events)
        }

    @Test
    fun `reports whether the user is logged in`() =
        runTest {
            assertFalse(GetUserLoggedInUseCase(authentication).invoke())

            authentication.loggedIn = true

            assertTrue(GetUserLoggedInUseCase(authentication).invoke())
        }

    @Test
    fun `rejects an empty email without calling the server`() =
        runTest {
            assertEquals(LoginResult.InvalidInput, RequestLoginLinkUseCase(authentication)(""))
            assertEquals(emptyList<String>(), recorder.events)
        }

    @Test
    fun `asks for the VPN permission, then notifications, then connects`() {
        permissions.vpnAllowed = false
        assertEquals(VpnPermission, postLogin())

        permissions.vpnAllowed = true
        permissions.notificationsAllowed = false
        assertEquals(NotificationPermission, postLogin())

        permissions.notificationsAllowed = true
        assertEquals(Connection, postLogin())
    }

    @Test
    fun `a login link logs in with its token`() =
        runTest {
            val destination = LoginWithLinkUseCase(authentication, postLogin)("piavpn:login?token=abc")

            assertEquals(Connection, destination)
            assertEquals(listOf("token(abc)"), recorder.events)
        }

    @Test
    fun `a login link goes to the login screen when the token is rejected`() =
        runTest {
            authentication.result = LoginResult.Failed

            assertEquals(LoginWithCredentials, LoginWithLinkUseCase(authentication, postLogin)("piavpn:login?token=abc"))
        }

    @Test
    fun `a login link skips the token when already logged in`() =
        runTest {
            authentication.loggedIn = true

            assertEquals(Connection, LoginWithLinkUseCase(authentication, postLogin)("piavpn:login?token=abc"))
            assertEquals(emptyList<String>(), recorder.events)
        }

    @Test
    fun `other links are ignored`() =
        runTest {
            assertNull(LoginWithLinkUseCase(authentication, postLogin)("https://example.com"))
        }
}