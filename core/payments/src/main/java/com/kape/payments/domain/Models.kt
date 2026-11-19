package com.kape.payments.domain

import kotlinx.serialization.Serializable

object BillingPeriod {
    const val MONTHLY = "P1M"
    const val YEARLY = "P1Y"
}

@Serializable
data class SubscriptionPlan(
    val id: String,
    val priceInMicros: Long,
    val formattedPrice: String,
    val billingPeriod: String,
    val currencyCode: String,
    val freeTrialDuration: String?,
    val plan: String,
)

data class OfferedPlans(
    val yearly: SubscriptionPlan,
    val monthly: SubscriptionPlan,
)

fun List<SubscriptionPlan>.offeredPlans(): OfferedPlans? {
    fun pick(period: String) =
        firstOrNull { it.billingPeriod == period && it.freeTrialDuration != null }
            ?: firstOrNull { it.billingPeriod == period && it.freeTrialDuration == null }
    val yearly = pick(BillingPeriod.YEARLY) ?: return null
    val monthly = pick(BillingPeriod.MONTHLY) ?: return null
    return OfferedPlans(yearly, monthly)
}

fun OfferedPlans.yearlySavingsPercent(): Int? {
    if (monthly.priceInMicros <= 0 || yearly.priceInMicros <= 0) return null
    val twelveMonths = monthly.priceInMicros * 12.0
    return Math.round((1 - yearly.priceInMicros / twelveMonths) * 100).toInt()
}

sealed interface BillingState {
    data object NotConnected : BillingState

    data object Ready : BillingState

    data object Unavailable : BillingState

    data object Disconnected : BillingState

    data class PlansLoaded(
        val plans: OfferedPlans,
    ) : BillingState

    data object PlansUnavailable : BillingState

    data object Purchased : BillingState

    data class PurchaseFailed(
        val reason: String,
    ) : BillingState
}