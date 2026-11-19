package com.kape.account.domain.usecase

import com.kape.account.domain.AccountDetails
import com.kape.account.domain.AccountDetailsRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class GetAccountDetailsUseCaseTest {
    private val account = AccountDetails("p1234567", isExpired = false, daysRemaining = 30, showExpiryWarning = false)

    @Test
    fun `retries until the account is fetched`() =
        runTest {
            val repository = FlakyRepository(failures = 3, account)

            assertEquals(account, GetAccountDetailsUseCase(repository)())
            assertEquals(4, repository.calls)
        }

    @Test
    fun `gives up after five attempts`() =
        runTest {
            val repository = FlakyRepository(failures = Int.MAX_VALUE, account)

            assertNull(GetAccountDetailsUseCase(repository)())
            assertEquals(5, repository.calls)
        }

    @Test
    fun `the subscription ends the remaining days from now`() {
        assertEquals(1_000L + 30 * 86_400_000L, account.expiresAtMillis(nowMillis = 1_000L))
    }
}

private class FlakyRepository(
    private val failures: Int,
    private val account: AccountDetails,
) : AccountDetailsRepository {
    var calls = 0

    override suspend fun getAccountDetails(): AccountDetails? = account.takeIf { calls++ >= failures }
}