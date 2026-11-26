package com.kape.login.domain.usecase

import com.kape.account.domain.AuthenticationRepository
import com.kape.contracts.IsUserLoggedInUseCase
import org.koin.core.annotation.Factory

@Factory(binds = [IsUserLoggedInUseCase::class])
class GetUserLoggedInUseCase(
    private val repository: AuthenticationRepository,
) : IsUserLoggedInUseCase {
    override suspend fun invoke(): Boolean = repository.isLoggedIn()
}