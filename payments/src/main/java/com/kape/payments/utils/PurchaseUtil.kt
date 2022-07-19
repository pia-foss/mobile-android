package com.kape.payments.utils

import android.content.Context

const val monthlyPlan = "MONTHLY"
const val yearlyPlan = "YEARLY"

interface PurchaseUtil {

    fun init(context: Context)

    fun loadProducts()

    fun purchaseSelectedProduct()

    fun selectProduct(isYearly: Boolean)

    fun getSelectedProductId(): String?

    fun getPurchaseUpdates()
}