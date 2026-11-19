package com.kape.account.domain

sealed interface LoginResult {
    data object Success : LoginResult

    data object InvalidInput : LoginResult

    data object Failed : LoginResult

    data object Expired : LoginResult

    data object Throttled : LoginResult

    data class ServiceUnavailable(
        val code: Int?,
        val message: String?,
    ) : LoginResult
}