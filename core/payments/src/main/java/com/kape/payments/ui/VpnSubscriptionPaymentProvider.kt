package com.kape.payments.ui

import android.app.Activity
import com.kape.payments.data.Subscription
import com.kape.payments.data.SubscriptionPlan
import com.kape.payments.utils.PurchaseHistoryState
import com.kape.payments.utils.PurchaseState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface VpnSubscriptionPaymentProvider {

    val purchaseState: MutableStateFlow<PurchaseState>

    val purchaseHistoryState: MutableStateFlow<PurchaseHistoryState>

    fun register(activity: Activity)

    @Deprecated("Deprecated in favor of SubscriptionPlan")
    fun getMonthlySubscription(): Subscription?

    @Deprecated("Deprecated in favor of SubscriptionPlan")
    fun getYearlySubscription(): Subscription?

    fun getMonthlySubscriptionPlan(): SubscriptionPlan?

    fun getYearlySubscriptionPlan(): SubscriptionPlan?

    fun getFreeTrialYearlySubscriptionPlan(): SubscriptionPlan?

    fun loadProducts()

    fun purchaseSelectedProduct(id: String)

    fun getPurchaseUpdates()

    fun getPurchaseHistory()

    fun hasActiveSubscription(): Flow<Boolean>

    fun isClientRegistered(): Boolean

    fun reset()
}