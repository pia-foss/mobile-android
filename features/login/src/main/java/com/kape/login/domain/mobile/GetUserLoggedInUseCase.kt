package com.kape.login.domain.mobile

class GetUserLoggedInUseCase(private val source: AuthenticationDataSource) {

    fun isUserLoggedIn() = source.isUserLoggedIn()
}