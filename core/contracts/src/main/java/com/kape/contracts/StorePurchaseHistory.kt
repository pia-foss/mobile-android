package com.kape.contracts

import kotlinx.coroutines.flow.StateFlow

interface StorePurchaseHistory {
    val state: StateFlow<PurchaseHistoryState>

    fun request()

    suspend fun onReceiptLoginSucceeded()
}

sealed interface PurchaseHistoryState {
    data object Default : PurchaseHistoryState

    data class Success(
        val purchaseToken: String,
        val productId: String,
    ) : PurchaseHistoryState

    data object Failed : PurchaseHistoryState
}