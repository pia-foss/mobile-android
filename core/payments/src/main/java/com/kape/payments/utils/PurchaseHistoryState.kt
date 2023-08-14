package com.kape.payments.utils

sealed class PurchaseHistoryState {

    data object Default : PurchaseHistoryState()

    data class PurchaseHistorySuccess(val purchaseToken: String, val productId: String) :
        PurchaseHistoryState()

    data object PurchaseHistoryFailed : PurchaseHistoryState()
}