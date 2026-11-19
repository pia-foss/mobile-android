package com.kape.login.domain.usecase

import com.kape.account.domain.AuthenticationRepository
import com.kape.account.domain.LoginResult
import com.kape.account.domain.usecase.GetPostLoginDestinationUseCase
import com.kape.data.ComposeDestination
import com.kape.data.LoginWithCredentials
import com.kape.login.domain.parseLoginLinkToken
import org.koin.core.annotation.Factory

@Factory
class LoginWithLinkUseCase(
    private val repository: AuthenticationRepository,
    private val getPostLoginDestination: GetPostLoginDestinationUseCase,
) {
    suspend operator fun invoke(url: String): ComposeDestination? {
        val token = parseLoginLinkToken(url) ?: return null
        if (repository.isLoggedIn()) return getPostLoginDestination()
        return when (repository.loginWithToken(token)) {
            LoginResult.Success -> getPostLoginDestination()
            else -> LoginWithCredentials
        }
    }
}