package com.kape.payments.utils

sealed class PurchaseState {
    object Default: PurchaseState()
    object InitSuccess : PurchaseState()
    object InitFailed : PurchaseState()
    object ProductsLoadedSuccess : PurchaseState()
    object ProductsLoadedFailed : PurchaseState()
    object PurchaseSuccess : PurchaseState()
    object PurchaseFailed : PurchaseState()
}