package com.kape.contracts

import com.kape.contracts.data.auth.ApiResult

interface AuthenticationDataSource {

    fun isUserLoggedIn(): Boolean

    suspend fun login(username: String, password: String): ApiResult

    suspend fun logout(): ApiResult

    suspend fun loginWithEmail(email: String): ApiResult

    suspend fun loginWithReceipt(
        receiptToken: String,
        productId: String,
        packageName: String,
    ): ApiResult

    suspend fun migrateToken(apiToken: String): ApiResult
}