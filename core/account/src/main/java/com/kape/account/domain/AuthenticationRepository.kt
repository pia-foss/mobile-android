package com.kape.account.domain

interface AuthenticationRepository {
    suspend fun isLoggedIn(): Boolean

    suspend fun loginWithCredentials(
        username: String,
        password: String,
    ): LoginResult

    suspend fun requestLoginLink(email: String): LoginResult

    suspend fun loginWithReceipt(
        purchaseToken: String,
        productId: String,
        packageName: String,
    ): LoginResult

    suspend fun loginWithToken(apiToken: String): LoginResult

    suspend fun logout(): Boolean
}