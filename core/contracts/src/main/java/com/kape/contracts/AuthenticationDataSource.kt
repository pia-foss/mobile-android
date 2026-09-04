package com.kape.contracts

import com.kape.data.auth.ApiResult

interface AuthenticationDataSource {
    // retryOnColdStart retries the check for callers that may race the Keystore right after boot
    // (e.g. auto-reconnect); other callers get an instant, non-retrying read.
    suspend fun isUserLoggedIn(retryOnColdStart: Boolean = false): Boolean

    suspend fun login(
        username: String,
        password: String,
    ): ApiResult

    suspend fun logout(): ApiResult

    suspend fun loginWithEmail(email: String): ApiResult

    suspend fun loginWithReceipt(
        receiptToken: String,
        productId: String,
        packageName: String,
    ): ApiResult

    suspend fun migrateToken(apiToken: String): ApiResult
}