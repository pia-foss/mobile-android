package com.kape.payments

import android.content.Context

interface PurchaseUtil {

    fun init(context: Context)

    fun loadProducts()

    fun purchaseSelectedProduct()

    fun selectProduct()

    fun getSelectedProductId(): String?

    fun getPurchaseUpdates()
}