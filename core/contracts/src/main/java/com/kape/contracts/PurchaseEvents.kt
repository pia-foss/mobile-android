package com.kape.contracts

interface PurchaseEvents {
    suspend fun purchaseStarted()

    suspend fun purchaseSucceeded()

    suspend fun purchaseFailed(reason: String)

    suspend fun signupRetried(
        attempt: Int,
        error: String,
    )
}