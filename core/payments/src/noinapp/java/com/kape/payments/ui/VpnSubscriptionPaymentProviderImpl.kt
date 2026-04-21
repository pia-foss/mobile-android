package com.kape.payments.ui

import android.app.Activity
import com.kape.payments.SubscriptionPrefs
import com.kape.payments.data.Subscription
import com.kape.payments.data.SubscriptionPlan
import com.kape.payments.utils.MONTHLY_SUBSCRIPTION
import com.kape.payments.utils.PurchaseHistoryState
import com.kape.payments.utils.PurchaseState
import com.kape.payments.utils.YEARLY_SUBSCRIPTION
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.annotation.Singleton

@Singleton([VpnSubscriptionPaymentProvider::class])
class VpnSubscriptionPaymentProviderImpl(
    private val prefs: SubscriptionPrefs,
    var activity: Activity? = null,
) : VpnSubscriptionPaymentProvider {
    override val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.NoInAppPurchase)
    override val purchaseHistoryState =
        MutableStateFlow<PurchaseHistoryState>(PurchaseHistoryState.Default)

    override fun register(activity: Activity) {
        this.activity = activity
    }

    override fun getMonthlySubscription(): Subscription =
        prefs.getVpnSubscriptions().first {
            it.plan.lowercase() == MONTHLY_SUBSCRIPTION.lowercase()
        }

    override fun getYearlySubscription(): Subscription =
        prefs.getVpnSubscriptions().first {
            it.plan.lowercase() == YEARLY_SUBSCRIPTION.lowercase()
        }

    override fun getMonthlySubscriptionPlan(): SubscriptionPlan? {
        // Subscriptions not supported
        return null
    }

    override fun getYearlySubscriptionPlan(): SubscriptionPlan? {
        // Subscriptions not supported
        return null
    }

    override fun getFreeTrialYearlySubscriptionPlan(): SubscriptionPlan? {
        // Subscriptions not supported
        return null
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

    override fun hasActiveSubscription(): Flow<Boolean> =
        callbackFlow {
            trySend(false)
            awaitClose { channel.close() }
        }

    override fun isClientRegistered(): Boolean = false

    override fun reset() {
        purchaseState.value = PurchaseState.NoInAppPurchase
    }
}