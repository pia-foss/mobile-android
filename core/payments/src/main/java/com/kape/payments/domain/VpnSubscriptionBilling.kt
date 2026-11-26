package com.kape.payments.domain

import android.app.Activity
import com.kape.contracts.PurchaseHistoryState
import com.kape.data.model.PurchaseData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface VpnSubscriptionBilling {
    val state: StateFlow<BillingState>

    val purchaseHistory: StateFlow<PurchaseHistoryState>

    val subscriptionStatusUpdates: Flow<String>

    fun connect(
        activity: Activity,
        onReady: () -> Unit = {},
    )

    fun isConnected(): Boolean

    suspend fun loadPlans()

    fun purchase(
        productId: String,
        activity: Activity,
    )

    fun requestPurchaseHistory()

    fun hasActiveSubscription(): Flow<Boolean>

    fun showPaymentIssueMessages(activity: Activity)

    suspend fun pendingPurchase(): PurchaseData?

    suspend fun clearPendingPurchase()

    suspend fun clearStoredData()

    fun reset()
}