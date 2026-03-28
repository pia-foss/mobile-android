package com.kape.login.domain.mobile

import com.kape.contracts.AuthenticationDataSource
import org.koin.core.annotation.Singleton

@Singleton
class GetUserLoggedInUseCase(private val source: AuthenticationDataSource) {

    fun isUserLoggedIn() = source.isUserLoggedIn()
}