package com.kape.payments.ui

import android.app.Activity
import com.kape.payments.data.Subscription
import com.kape.payments.utils.PurchaseHistoryState
import com.kape.payments.utils.PurchaseState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface VpnSubscriptionPaymentProvider {

    val purchaseState: MutableStateFlow<PurchaseState>

    val purchaseHistoryState: MutableStateFlow<PurchaseHistoryState>

    fun register(activity: Activity)

    fun getMonthlySubscription(): Subscription?

    fun getYearlySubscription(): Subscription?

    fun loadProducts()

    fun purchaseSelectedProduct(id: String)

    fun getPurchaseUpdates()

    fun getPurchaseHistory()

    fun hasActiveSubscription(): Flow<Boolean>

    fun isClientRegistered(): Boolean

    fun reset()
}