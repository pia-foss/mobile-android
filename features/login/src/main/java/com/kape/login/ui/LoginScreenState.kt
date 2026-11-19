package com.kape.login.ui

import com.kape.account.domain.LoginResult
import com.kape.account.domain.usecase.ReceiptLoginProgress

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

fun LoginResult.toScreenState(): LoginScreenState =
    when (this) {
        LoginResult.Success -> SUCCESS
        LoginResult.InvalidInput -> INVALID
        LoginResult.Failed -> FAILED
        LoginResult.Expired -> EXPIRED
        LoginResult.Throttled -> THROTTLED
        is LoginResult.ServiceUnavailable -> SERVICE_UNAVAILABLE
    }

fun LoginResult.toReceiptScreenState(): LoginScreenState =
    when (this) {
        LoginResult.Failed -> RECEIPT_FAILED
        else -> toScreenState()
    }

sealed class LoginError {
    data object Invalid : LoginError()

    data object Throttled : LoginError()

    data object Failed : LoginError()

    data object Expired : LoginError()

    data object ServiceUnavailable : LoginError()

    data object ReceiptFailed : LoginError()
}

fun ReceiptLoginProgress.toScreenState(): LoginScreenState? =
    when (this) {
        ReceiptLoginProgress.Idle -> IDLE
        ReceiptLoginProgress.Loading -> LOADING
        ReceiptLoginProgress.NoPurchase -> RECEIPT_FAILED
        is ReceiptLoginProgress.Failed -> result.toReceiptScreenState()
        is ReceiptLoginProgress.Succeeded -> null
    }