package com.kape.payments.ui

import android.app.Activity
import com.kape.payments.SubscriptionPrefs
import com.kape.payments.data.Subscription
import com.kape.payments.utils.PurchaseHistoryState
import com.kape.payments.utils.PurchaseState
import com.kape.payments.utils.monthlySubscription
import com.kape.payments.utils.yearlySubscription
import kotlinx.coroutines.flow.MutableStateFlow

class PaymentProviderImpl(private val prefs: SubscriptionPrefs, var activity: Activity? = null) :
    PaymentProvider {

    override val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.NoInAppPurchase)
    override val purchaseHistoryState =
        MutableStateFlow<PurchaseHistoryState>(PurchaseHistoryState.Default)

    override fun register(activity: Activity) {
        this.activity = activity
    }

    override fun getMonthlySubscription(): Subscription = prefs.getSubscriptions().first {
        it.plan.lowercase() == monthlySubscription.lowercase()
    }

    override fun getYearlySubscription(): Subscription = prefs.getSubscriptions().first {
        it.plan.lowercase() == yearlySubscription.lowercase()
    }

    override fun loadProducts() {
        // no-op
    }

    override fun purchaseSelectedProduct(id: String) {
        // no-op
    }

    override fun getPurchaseUpdates() {
        // no-op
    }

    override fun getPurchaseHistory() {
        // no-op
    }
}