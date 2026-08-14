package com.kape.login.utils

data class LoginScreenState(
    val idle: Boolean,
    val loading: Boolean,
    val error: LoginError?,
    val flowCompleted: Boolean,
)

val IDLE = LoginScreenState(idle = true, loading = false, error = null, flowCompleted = false)
val LOADING = LoginScreenState(idle = false, loading = true, error = null, flowCompleted = false)
val SUCCESS = LoginScreenState(idle = true, loading = false, error = null, flowCompleted = true)
val THROTTLED = LoginScreenState(idle = true, loading = false, error = LoginError.Throttled, flowCompleted = false)
val FAILED = LoginScreenState(idle = true, loading = false, error = LoginError.Failed, flowCompleted = false)
val EXPIRED = LoginScreenState(idle = true, loading = false, error = LoginError.Expired, flowCompleted = false)
val INVALID = LoginScreenState(idle = true, loading = false, error = LoginError.Invalid, flowCompleted = false)
val SERVICE_UNAVAILABLE =
    LoginScreenState(idle = true, loading = false, error = LoginError.ServiceUnavailable, flowCompleted = false)
val RECEIPT_FAILED =
    LoginScreenState(idle = true, loading = false, error = LoginError.ReceiptFailed, flowCompleted = false)

fun getScreenState(state: LoginState): LoginScreenState =
    when (state) {
        LoginState.Expired -> EXPIRED
        LoginState.Failed -> FAILED
        LoginState.Successful -> SUCCESS
        is LoginState.Throttled -> THROTTLED
        is LoginState.ServiceUnavailable -> SERVICE_UNAVAILABLE
    }

fun getReceiptScreenState(state: LoginState): LoginScreenState =
    when (state) {
        LoginState.Failed -> RECEIPT_FAILED
        else -> getScreenState(state)
    }

sealed class LoginError {
    data object Invalid : LoginError()

    data object Throttled : LoginError()

    data object Failed : LoginError()

    data object Expired : LoginError()

    data object ServiceUnavailable : LoginError()

    data object ReceiptFailed : LoginError()
}