package com.kape.payments.domain

import com.kape.payments.models.Subscription
import com.kape.payments.utils.PurchaseState
import kotlinx.coroutines.flow.MutableStateFlow


interface BillingDataSource {

    val purchaseState: MutableStateFlow<PurchaseState>

    fun getMonthlySubscription(): Subscription

    fun getYearlySubscription(): Subscription

    fun loadProducts()

    fun purchaseSelectedProduct(id: String)
}