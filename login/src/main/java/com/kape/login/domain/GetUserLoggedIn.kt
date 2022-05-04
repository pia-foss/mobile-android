package com.kape.login.domain

import com.kape.login.data.AuthenticationDataSource

class GetUserLoggedIn(private val source: AuthenticationDataSource) {

    fun isUserLoggedIn() = source.isUserLoggedIn()
}