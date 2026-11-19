package com.kape.payments.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal fun plan(
    period: String,
    priceInMicros: Long,
    freeTrial: String? = null,
) = SubscriptionPlan(
    id = "pia_$period${freeTrial.orEmpty()}",
    priceInMicros = priceInMicros,
    formattedPrice = "$${priceInMicros / 1_000_000.0}",
    billingPeriod = period,
    currencyCode = "USD",
    freeTrialDuration = freeTrial,
    plan = if (period == BillingPeriod.YEARLY) "YEARLY" else "MONTHLY",
)

internal class OfferedPlansTest {
    private val monthly = plan(BillingPeriod.MONTHLY, 11_990_000)
    private val yearly = plan(BillingPeriod.YEARLY, 59_990_000)
    private val yearlyTrial = plan(BillingPeriod.YEARLY, 59_990_000, freeTrial = "P7D")

    @Test
    fun `offers the yearly and monthly plans`() {
        assertEquals(OfferedPlans(yearly, monthly), listOf(monthly, yearly).offeredPlans())
    }

    @Test
    fun `prefers a plan's free trial version`() {
        assertEquals(yearlyTrial, listOf(yearly, yearlyTrial, monthly).offeredPlans()?.yearly)
    }

    @Test
    fun `offers nothing when a period is missing`() {
        assertNull(listOf(yearly).offeredPlans())
    }

    @Test
    fun `yearly savings compare to twelve monthly payments`() {
        assertEquals(58, OfferedPlans(yearly, monthly).yearlySavingsPercent())
    }

    @Test
    fun `no savings without real prices`() {
        assertNull(OfferedPlans(yearly, plan(BillingPeriod.MONTHLY, 0)).yearlySavingsPercent())
        assertNull(OfferedPlans(plan(BillingPeriod.YEARLY, 0), monthly).yearlySavingsPercent())
    }

    @Test
    fun `offers nothing without a monthly plan`() {
        assertNull(listOf(yearly, yearlyTrial).offeredPlans())
    }

    @Test
    fun `ignores plans with other billing periods`() {
        assertEquals(OfferedPlans(yearly, monthly), listOf(plan("P3M", 1_000_000), yearly, monthly).offeredPlans())
    }

    @Test
    fun `a yearly plan dearer than twelve months shows negative savings`() {
        val plans = OfferedPlans(plan(BillingPeriod.YEARLY, 15_000_000), plan(BillingPeriod.MONTHLY, 1_000_000))

        assertEquals(-25, plans.yearlySavingsPercent())
    }
}