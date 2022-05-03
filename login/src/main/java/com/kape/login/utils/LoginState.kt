package com.kape.login.utils

sealed class LoginState {
    object Successful: LoginState() {
        override fun toString() = "LoginState.Successful"
    }
    object Failed: LoginState() {
        override fun toString() = "LoginState.Failed"
    }
    object Expired: LoginState() {
        override fun toString() = "LoginState.Expired"
    }
    object Throttled: LoginState() {
        override fun toString() = "LoginState.Throttled"
    }
}