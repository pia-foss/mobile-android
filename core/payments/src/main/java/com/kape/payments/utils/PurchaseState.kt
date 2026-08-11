package com.kape.payments.utils

sealed class PurchaseState {
    data object Default : PurchaseState()

    data object InitSuccess : PurchaseState()

    data object InitFailed : PurchaseState()

    data object ProductsLoadedSuccess : PurchaseState()

    data object ProductsLoadedFailed : PurchaseState()

    data object PurchaseSuccess : PurchaseState()

    data class PurchaseFailed(
        val reason: String,
    ) : PurchaseState()

    data object NoInAppPurchase : PurchaseState()

    data object Disconnected : PurchaseState()
}