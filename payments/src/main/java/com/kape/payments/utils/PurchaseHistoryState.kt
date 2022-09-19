package com.kape.payments.utils

sealed class PurchaseHistoryState {

    object Default : PurchaseHistoryState()

    data class PurchaseHistorySuccess(val purchaseToken: String, val productId: String) :
        PurchaseHistoryState()

    object PurchaseHistoryFailed : PurchaseHistoryState()

}