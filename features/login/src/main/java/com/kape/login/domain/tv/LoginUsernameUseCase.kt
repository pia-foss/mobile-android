package com.kape.login.domain.tv

import org.koin.core.annotation.Singleton

@Singleton
class LoginUsernameUseCase {

    private var loginUsername: String = ""

    fun setLoginUsername(loginUsername: String) {
        this.loginUsername = loginUsername
    }

    fun getLoginUsername(): String =
        loginUsername
}