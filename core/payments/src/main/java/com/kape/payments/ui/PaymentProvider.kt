package com.kape.payments.ui

import android.app.Activity
import com.kape.payments.data.models.Subscription
import com.kape.payments.utils.PurchaseHistoryState
import com.kape.payments.utils.PurchaseState
import kotlinx.coroutines.flow.MutableStateFlow


interface PaymentProvider {

    val purchaseState: MutableStateFlow<PurchaseState>

    val purchaseHistoryState: MutableStateFlow<PurchaseHistoryState>

    fun register(activity: Activity)

    fun getMonthlySubscription(): Subscription

    fun getYearlySubscription(): Subscription

    fun loadProducts()

    fun purchaseSelectedProduct(id: String)

    fun getPurchaseUpdates()

    fun getPurchaseHistory()
}