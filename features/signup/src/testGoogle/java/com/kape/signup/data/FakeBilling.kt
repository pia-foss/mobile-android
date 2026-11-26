package com.kape.signup.data

import android.app.Activity
import com.kape.contracts.PurchaseEvents
import com.kape.contracts.PurchaseHistoryState
import com.kape.data.model.PurchaseData
import com.kape.payments.domain.BillingState
import com.kape.payments.domain.VpnSubscriptionBilling
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

internal class FakeBilling : VpnSubscriptionBilling {
    override val state = MutableStateFlow<BillingState>(BillingState.NotConnected)
    override val purchaseHistory = MutableStateFlow<PurchaseHistoryState>(PurchaseHistoryState.Default)
    override val subscriptionStatusUpdates = emptyFlow<String>()
    var pending: PurchaseData? = PurchaseData("token", "pia_yearly", "order-1")
    var connected = true
    val purchases = mutableListOf<String>()

    override fun connect(
        activity: Activity,
        onReady: () -> Unit,
    ) {
        connected = true
        onReady()
    }

    override fun isConnected() = connected

    override suspend fun loadPlans() = Unit

    override fun purchase(
        productId: String,
        activity: Activity,
    ) {
        purchases += productId
    }

    override fun requestPurchaseHistory() = Unit

    override fun hasActiveSubscription() = flowOf(false)

    override fun showPaymentIssueMessages(activity: Activity) = Unit

    override suspend fun pendingPurchase() = pending

    override suspend fun clearPendingPurchase() {
        pending = null
    }

    override suspend fun clearStoredData() = Unit

    override fun reset() = Unit
}

internal class RecordingEvents : PurchaseEvents {
    val events = mutableListOf<String>()

    override suspend fun purchaseStarted() {
        events += "started"
    }

    override suspend fun purchaseSucceeded() {
        events += "succeeded"
    }

    override suspend fun purchaseFailed(reason: String) {
        events += "failed: $reason"
    }

    override suspend fun signupRetried(
        attempt: Int,
        error: String,
    ) {
        events += "retry $attempt: $error"
    }
}