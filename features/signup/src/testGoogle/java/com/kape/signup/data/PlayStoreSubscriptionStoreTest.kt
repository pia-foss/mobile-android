package com.kape.signup.data

import com.kape.payments.domain.BillingPeriod
import com.kape.payments.domain.BillingState
import com.kape.payments.domain.OfferedPlans
import com.kape.payments.domain.SubscriptionPlan
import com.kape.signup.domain.StoreOffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class PlayStoreSubscriptionStoreTest {
    private val billing = FakeBilling()
    private val events = RecordingEvents()
    private val store =
        PlayStoreSubscriptionStore(billing, { "$5.00/mo" }, events, CoroutineScope(UnconfinedTestDispatcher()))

    private val yearly = SubscriptionPlan("pia_yearly", 60_000_000, "$60.00", BillingPeriod.YEARLY, "USD", "P7D", "YEARLY")
    private val monthly = SubscriptionPlan("pia_monthly", 10_000_000, "$10.00", BillingPeriod.MONTHLY, "USD", null, "MONTHLY")

    @Test
    fun `offers the loaded plans with the yearly price per month and savings`() {
        billing.state.value = BillingState.PlansLoaded(OfferedPlans(yearly, monthly))

        val offer = store.offer.value as StoreOffer.Plans
        assertEquals("Yearly", offer.yearly.period)
        assertEquals(true, offer.yearly.hasFreeTrial)
        assertEquals("$5.00/mo", offer.yearly.secondaryPrice)
        assertEquals(50, offer.yearly.saving)
        assertEquals("Monthly", offer.monthly.period)
        assertEquals(false, offer.monthly.hasFreeTrial)
    }

    @Test
    fun `reports plans as unavailable when Play can't provide them`() {
        billing.state.value = BillingState.PlansUnavailable

        assertEquals(StoreOffer.PlansUnavailable, store.offer.value)
    }

    @Test
    fun `a purchase moves signup on and is recorded`() =
        runTest {
            val completed = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { store.purchaseCompleted.first() }

            billing.state.value = BillingState.Purchased

            assertEquals(true, completed.isCompleted)
            assertEquals(listOf("succeeded"), events.events)
        }

    @Test
    fun `a failed purchase is recorded`() {
        billing.state.value = BillingState.PurchaseFailed("declined")

        assertEquals(listOf("failed: declined"), events.events)
    }
}