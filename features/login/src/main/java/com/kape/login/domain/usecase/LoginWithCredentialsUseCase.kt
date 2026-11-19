package com.kape.login.domain.usecase

import com.kape.account.domain.AuthenticationRepository
import com.kape.account.domain.LoginResult
import org.koin.core.annotation.Factory

@Factory
class LoginWithCredentialsUseCase(
    private val repository: AuthenticationRepository,
) {
    suspend operator fun invoke(
        username: String,
        password: String,
    ): LoginResult {
        if (username.isEmpty() || password.isEmpty()) return LoginResult.InvalidInput
        return repository.loginWithCredentials(username, password)
    }
}