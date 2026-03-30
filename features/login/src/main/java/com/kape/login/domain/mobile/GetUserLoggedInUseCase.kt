package com.kape.login.domain.mobile

import com.kape.contracts.AuthenticationDataSource
import com.kape.contracts.IsUserLoggedInUseCase
import org.koin.core.annotation.Singleton

@Singleton
class GetUserLoggedInUseCase(private val source: AuthenticationDataSource) : IsUserLoggedInUseCase {

    override fun invoke() = source.isUserLoggedIn()
}