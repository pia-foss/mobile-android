package com.kape.login.utils

// Server/network/throttling failures, as opposed to account-state errors (invalid credentials, expired subscription).
interface QualifyingFailure {
    val code: Int?
    val message: String?
}

sealed class LoginState {
    object Successful : LoginState() {
        override fun toString() = "LoginState.Successful"
    }

    object Failed : LoginState() {
        override fun toString() = "LoginState.Failed"
    }

    object Expired : LoginState() {
        override fun toString() = "LoginState.Expired"
    }

    object Throttled : LoginState() {
        override fun toString() = "LoginState.Throttled"
    }

    data class ServiceUnavailable(
        override val code: Int?,
        override val message: String?,
    ) : LoginState(),
        QualifyingFailure {
        override fun toString() = "LoginState.ServiceUnavailable"
    }
}