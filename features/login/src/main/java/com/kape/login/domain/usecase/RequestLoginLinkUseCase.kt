package com.kape.login.domain.usecase

import com.kape.account.domain.AuthenticationRepository
import com.kape.account.domain.LoginResult
import org.koin.core.annotation.Factory

@Factory
class RequestLoginLinkUseCase(
    private val repository: AuthenticationRepository,
) {
    suspend operator fun invoke(email: String): LoginResult {
        if (email.isEmpty()) return LoginResult.InvalidInput
        return repository.requestLoginLink(email)
    }
}