package com.kape.account.data

import com.kape.account.domain.AccountDetails
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.AccountInformation
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy

internal class ApiAccountDetailsRepositoryTest {
    private val api = FakeAccountApi()
    private val repository = ApiAccountDetailsRepository(api)

    @Test
    fun `maps the account information to account details`() =
        runTest {
            api.details = accountInformation(username = "p1234567", expired = true, daysRemaining = 3, expireAlert = true)

            assertEquals(
                AccountDetails(username = "p1234567", isExpired = true, daysRemaining = 3, showExpiryWarning = true),
                repository.getAccountDetails(),
            )
        }

    @Test
    fun `returns null when the request returns no account`() =
        runTest {
            api.errors = listOf(AccountRequestError(500, "error"))

            assertNull(repository.getAccountDetails())
        }

    private fun accountInformation(
        username: String,
        expired: Boolean,
        daysRemaining: Int,
        expireAlert: Boolean,
    ) = AccountInformation(
        active = !expired,
        canInvite = false,
        canceled = false,
        daysRemaining = daysRemaining,
        email = "user@example.com",
        expirationTime = 0,
        expireAlert = expireAlert,
        expired = expired,
        needsPayment = false,
        plan = "monthly",
        productId = "product",
        recurring = true,
        renewUrl = "https://example.com",
        renewable = true,
        username = username,
    )
}

private val unusedApi =
    Proxy.newProxyInstance(AndroidAccountAPI::class.java.classLoader, arrayOf(AndroidAccountAPI::class.java)) { _, method, _ ->
        error("Unexpected call to ${method.name}")
    } as AndroidAccountAPI

private class FakeAccountApi : AndroidAccountAPI by unusedApi {
    var details: AccountInformation? = null
    var errors: List<AccountRequestError> = emptyList()

    override fun accountDetails(callback: (details: AccountInformation?, errors: List<AccountRequestError>) -> Unit) {
        callback(details, errors)
    }
}