package com.kape.account.domain.usecase

import com.kape.account.domain.AccountDetails
import com.kape.account.domain.AccountDetailsRepository
import kotlinx.coroutines.delay
import org.koin.core.annotation.Factory

private const val ATTEMPTS = 5
private const val RETRY_DELAY_MS = 300L

@Factory
class GetAccountDetailsUseCase(
    private val repository: AccountDetailsRepository,
) {
    suspend operator fun invoke(): AccountDetails? {
        repeat(ATTEMPTS - 1) {
            repository.getAccountDetails()?.let { return it }
            delay(RETRY_DELAY_MS)
        }
        return repository.getAccountDetails()
    }
}