package com.kape.login.domain.mobile

import com.kape.utils.ApiResult
import kotlinx.coroutines.flow.Flow

interface AuthenticationDataSource {

    fun isUserLoggedIn(): Boolean

    fun login(username: String, password: String): Flow<ApiResult>

    fun logout(): Flow<ApiResult>

    fun loginWithEmail(email: String): Flow<ApiResult>

    fun loginWithReceipt(
        receiptToken: String,
        productId: String,
        packageName: String,
    ): Flow<ApiResult>

    fun migrateToken(apiToken: String): Flow<ApiResult>
}