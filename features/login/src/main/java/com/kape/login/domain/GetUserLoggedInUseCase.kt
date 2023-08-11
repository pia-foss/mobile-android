package com.kape.login.domain

class GetUserLoggedInUseCase(private val source: AuthenticationDataSource) {

    fun isUserLoggedIn() = source.isUserLoggedIn()
}