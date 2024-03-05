package com.kape.login.domain.tv

class LoginUsernameUseCase {

    private var loginUsername: String = ""

    fun setLoginUsername(loginUsername: String) {
        this.loginUsername = loginUsername
    }

    fun getLoginUsername(): String =
        loginUsername
}