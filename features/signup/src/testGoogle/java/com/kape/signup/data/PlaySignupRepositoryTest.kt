package com.kape.signup.data

import com.kape.account.domain.AuthenticationRepository
import com.kape.account.domain.LoginResult
import com.kape.data.model.PurchaseData
import com.kape.signup.domain.Credentials
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class PlaySignupRepositoryTest {
    private val credentials = Credentials("active", "p1234567", "secret")
    private val billing = FakeBilling()
    private val accounts = FakeAccounts()
    private val authentication = FakeAuthentication()
    private val events = RecordingEvents()
    private val repository = PlaySignupRepository(billing, accounts, authentication, { "device-hash" }, events)

    @Test
    fun `creates the account, logs in, sets the email and forgets the purchase`() =
        runTest {
            accounts.results = mutableListOf(AccountCreation.Created(credentials))

            assertEquals(credentials, repository.signUp("a@b.c"))
            assertEquals(listOf("a@b.c"), accounts.emails)
            assertEquals(listOf("p1234567"), authentication.logins)
            assertNull(billing.pending)
        }

    @Test
    fun `retries network failures, then succeeds`() =
        runTest {
            accounts.results =
                mutableListOf(
                    AccountCreation.NetworkError("timeout"),
                    AccountCreation.NetworkError("timeout"),
                    AccountCreation.Created(credentials),
                )

            assertEquals(credentials, repository.signUp("a@b.c"))
            assertEquals(listOf("retry 1: timeout", "retry 2: timeout"), events.events)
        }

    @Test
    fun `gives up after three retries`() =
        runTest {
            accounts.results = MutableList(4) { AccountCreation.NetworkError("offline") }

            assertNull(repository.signUp("a@b.c"))
            assertEquals(4, accounts.attempts)
        }

    @Test
    fun `doesn't retry a rejected receipt`() =
        runTest {
            accounts.results = mutableListOf(AccountCreation.Rejected)

            assertNull(repository.signUp("a@b.c"))
            assertEquals(1, accounts.attempts)
        }

    @Test
    fun `keeps the purchase when the login fails, so signup can be resumed`() =
        runTest {
            accounts.results = mutableListOf(AccountCreation.Created(credentials))
            authentication.result = LoginResult.ServiceUnavailable(503, null)

            assertNull(repository.signUp("a@b.c"))
            assertEquals("token", billing.pending?.token)
        }

    @Test
    fun `there's nothing to sign up for without a purchase`() =
        runTest {
            billing.pending = null

            assertNull(repository.signUp("a@b.c"))
            assertEquals(0, accounts.attempts)
        }
}

private class FakeAccounts : AccountSignupDataSource {
    var results = mutableListOf<AccountCreation>()
    var attempts = 0
    val emails = mutableListOf<String>()

    override suspend fun createAccount(
        purchase: PurchaseData,
        obfuscatedDeviceId: String,
    ): AccountCreation {
        attempts++
        return results.removeAt(0)
    }

    override suspend fun setEmail(email: String): Boolean {
        emails += email
        return true
    }
}

private class FakeAuthentication : AuthenticationRepository {
    var result: LoginResult = LoginResult.Success
    val logins = mutableListOf<String>()

    override suspend fun isLoggedIn() = false

    override suspend fun loginWithCredentials(
        username: String,
        password: String,
    ) = result.also { logins += username }

    override suspend fun requestLoginLink(email: String) = result

    override suspend fun loginWithReceipt(
        purchaseToken: String,
        productId: String,
        packageName: String,
    ) = result

    override suspend fun loginWithToken(apiToken: String) = result

    override suspend fun logout() = true
}