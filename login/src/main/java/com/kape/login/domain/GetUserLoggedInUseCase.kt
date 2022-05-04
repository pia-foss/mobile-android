package com.kape.login.domain

import com.kape.login.data.AuthenticationDataSource

class GetUserLoggedInUseCase(private val source: AuthenticationDataSource) {

    fun isUserLoggedIn() = source.isUserLoggedIn()
}