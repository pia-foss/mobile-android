package com.kape.signup.data

import android.app.Activity
import com.kape.contracts.PurchaseEvents
import com.kape.contracts.SubscriptionBilling
import com.kape.contracts.di.Qualifiers
import com.kape.payments.domain.BillingState
import com.kape.payments.domain.OfferedPlans
import com.kape.payments.domain.SubscriptionPlan
import com.kape.payments.domain.VpnSubscriptionBilling
import com.kape.payments.domain.yearlySavingsPercent
import com.kape.signup.domain.Plan
import com.kape.signup.domain.StoreOffer
import com.kape.signup.domain.SubscriptionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

private val BILLING_READY_TIMEOUT = 5.seconds

fun interface YearlyPerMonthFormatter {
    fun format(yearly: SubscriptionPlan): String
}

@Singleton(binds = [SubscriptionStore::class, SubscriptionBilling::class])
class PlayStoreSubscriptionStore(
    private val billing: VpnSubscriptionBilling,
    private val yearlyPerMonth: YearlyPerMonthFormatter,
    private val events: PurchaseEvents,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) : SubscriptionStore,
    SubscriptionBilling {
    private val _offer = MutableStateFlow<StoreOffer>(StoreOffer.None)
    override val offer: StateFlow<StoreOffer> = _offer.asStateFlow()
    private val _purchaseCompleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val purchaseCompleted: Flow<Unit> = _purchaseCompleted.asSharedFlow()

    init {
        appScope.launch {
            billing.state.collect { state ->
                when (state) {
                    is BillingState.PlansLoaded -> _offer.value = state.plans.toOffer()
                    BillingState.Unavailable, BillingState.PlansUnavailable -> _offer.value = StoreOffer.PlansUnavailable
                    BillingState.Purchased -> {
                        events.purchaseSucceeded()
                        _purchaseCompleted.emit(Unit)
                    }
                    is BillingState.PurchaseFailed -> events.purchaseFailed(state.reason)
                    BillingState.NotConnected, BillingState.Ready, BillingState.Disconnected -> Unit
                }
            }
        }
    }

    override fun hasActiveSubscription(): Flow<Boolean> = billing.hasActiveSubscription()

    override suspend fun hasResumablePurchase(): Boolean = billing.pendingPurchase() != null

    override fun register(activity: Activity) {
        if (!billing.isConnected()) billing.connect(activity)
    }

    override fun loadPlans(activity: Activity) {
        _offer.value = StoreOffer.Loading
        // Waits for the connection: loading right after connecting failed.
        if (billing.isConnected()) {
            appScope.launch { billing.loadPlans() }
        } else {
            billing.connect(activity) { appScope.launch { billing.loadPlans() } }
        }
    }

    override fun purchase(
        planId: String,
        activity: Activity,
    ) {
        appScope.launch { events.purchaseStarted() }
        billing.purchase(planId, activity)
    }

    override fun reset() = billing.reset()

    override suspend fun initialize() = Unit

    override suspend fun awaitReady(activity: Activity) {
        register(activity)
        withTimeoutOrNull(BILLING_READY_TIMEOUT) {
            billing.state.first { it == BillingState.Ready || it == BillingState.Unavailable || it == BillingState.Disconnected }
        }
    }

    override fun loadPrices(activity: Activity) = loadPlans(activity)

    override fun isAvailable(): Boolean = billing.isConnected()

    private fun OfferedPlans.toOffer(): StoreOffer.Plans =
        StoreOffer.Plans(
            yearly =
                yearly.toPlan().copy(
                    secondaryPrice = yearlyPerMonth.format(yearly),
                    saving = yearlySavingsPercent(),
                ),
            monthly = monthly.toPlan(),
        )

    private fun SubscriptionPlan.toPlan() =
        Plan(
            id = id,
            period = plan.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) },
            hasFreeTrial = !freeTrialDuration.isNullOrBlank(),
            mainPrice = formattedPrice,
            freeTrialDuration = freeTrialDuration,
        )
}