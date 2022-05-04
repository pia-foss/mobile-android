package com.kape.core

sealed class ApiResult {
    object Success : ApiResult() {
        override fun toString() = "ApiResult.Success"
    }

    data class Error(val error: ApiError) : ApiResult() {
        override fun toString() = "$error"
    }
}

sealed class ApiError {
    object AuthFailed : ApiError() {
        override fun toString() = "ApiError.AuthFailed"
    }

    object AccountExpired : ApiError() {
        override fun toString() = "ApiError.AccountExpired"
    }

    object Throttled : ApiError() {
        override fun toString() = "ApiError.Throttled"
    }

    object Unknown : ApiError() {
        override fun toString() = "ApiError.Unknown"
    }
}

fun getApiError(code: Int?) = when (code) {
    401 -> ApiError.AuthFailed
    402 -> ApiError.AccountExpired
    429 -> ApiError.Throttled
    else -> ApiError.Unknown
}
