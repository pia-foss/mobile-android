package com.kape.payments.data

import android.app.Activity
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.InAppMessageResponseListener
import com.android.billingclient.api.InAppMessageResult
import com.android.billingclient.api.Purchase
import com.kape.contracts.PurchaseHistoryState
import com.kape.data.model.PurchaseData
import com.kape.data.model.Subscription
import com.kape.payments.data.FakePlayBilling.Companion.phase
import com.kape.payments.data.FakePlayBilling.Companion.product
import com.kape.payments.data.FakePlayBilling.Companion.purchase
import com.kape.payments.domain.BillingPeriod
import com.kape.payments.domain.BillingState
import com.kape.payments.domain.OfferedPlans
import com.kape.payments.domain.SubscriptionPlan
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PlayStoreVpnSubscriptionBillingTest {
    private val play = FakePlayBilling()
    private val prefs = mockk<SubscriptionPrefs>(relaxed = true)
    private var fetched: List<Subscription>? = null
    private val billing = PlayStoreVpnSubscriptionBilling(prefs, { fetched }, TestScope(UnconfinedTestDispatcher()))
    private val activity = mockk<Activity>()

    private val subscription = Subscription("pia_yearly", false, "yearly", "59.99", null)
    private val monthlySubscription = Subscription("pia_monthly", false, "monthly", "11.99", null)

    private val yearlyPlan = SubscriptionPlan("pia_yearly", 59_990_000, "$59.99", BillingPeriod.YEARLY, "USD", "P1W", "YEARLY")
    private val monthlyPlan = SubscriptionPlan("pia_monthly", 11_990_000, "$11.99", BillingPeriod.MONTHLY, "USD", null, "MONTHLY")

    @AfterEach
    fun tearDown() = play.close()

    private suspend fun connectAndLoadPlans() {
        play.connectsWith(BillingResponseCode.OK)
        billing.connect(activity) {}
        coEvery { prefs.subscriptions() } returns listOf(subscription, monthlySubscription)
        play.returnsProducts(
            product("pia_yearly", phase(0, "P1W"), phase(59_990_000, BillingPeriod.YEARLY, "$59.99")),
            product("pia_monthly", phase(11_990_000, BillingPeriod.MONTHLY, "$11.99")),
            product("not_sold", phase(1_000_000, BillingPeriod.MONTHLY)),
        )
        billing.loadPlans()
    }

    @Test
    fun `starts disconnected`() {
        assertEquals(BillingState.NotConnected, billing.state.value)
        assertEquals(PurchaseHistoryState.Default, billing.purchaseHistory.value)
        assertFalse(billing.isConnected())
    }

    @Test
    fun `loading plans stores the fetched subscriptions, but offers none before connecting`() =
        runTest {
            fetched = listOf(subscription)
            coEvery { prefs.subscriptions() } returns listOf(subscription)

            billing.loadPlans()

            coVerify { prefs.storeSubscriptions(listOf(subscription)) }
            assertEquals(BillingState.PlansUnavailable, billing.state.value)
        }

    @Test
    fun `keeps the stored subscriptions when fetching them fails`() =
        runTest {
            fetched = null
            coEvery { prefs.subscriptions() } returns emptyList()

            billing.loadPlans()

            coVerify(exactly = 0) { prefs.storeSubscriptions(any()) }
            assertEquals(BillingState.PlansUnavailable, billing.state.value)
        }

    @Test
    fun `purchasing before connecting fails`() {
        billing.purchase("pia_yearly", mockk())

        assertEquals(BillingState.PurchaseFailed("Billing not ready for pia_yearly"), billing.state.value)
    }

    @Test
    fun `purchase history fails before connecting`() {
        billing.requestPurchaseHistory()

        assertEquals(PurchaseHistoryState.Failed, billing.purchaseHistory.value)
    }

    @Test
    fun `there's no active subscription before connecting`() =
        runTest {
            assertEquals(listOf(false), billing.hasActiveSubscription().toList())
        }

    @Test
    fun `resetting returns to not connected`() {
        billing.purchase("pia_yearly", mockk())

        billing.reset()

        assertEquals(BillingState.NotConnected, billing.state.value)
    }

    @Test
    fun `pending purchase and stored data come from the prefs`() =
        runTest {
            val purchase = PurchaseData("token", "pia_yearly", "order")
            coEvery { prefs.purchase() } returns purchase

            assertEquals(purchase, billing.pendingPurchase())
            billing.clearPendingPurchase()
            billing.clearStoredData()

            coVerify { prefs.removePurchase() }
            coVerify { prefs.clear() }
            assertTrue(billing.state.value is BillingState.NotConnected)
        }

    @Test
    fun `connecting reports ready, unavailable or disconnected`() {
        var ready = false
        play.connectsWith(BillingResponseCode.OK)
        billing.connect(activity) { ready = true }
        assertEquals(BillingState.Ready, billing.state.value)
        assertTrue(ready)

        play.connectsWith(BillingResponseCode.BILLING_UNAVAILABLE)
        billing.connect(activity) {}
        assertEquals(BillingState.Unavailable, billing.state.value)

        play.disconnects()
        billing.connect(activity) {}
        assertEquals(BillingState.Disconnected, billing.state.value)
    }

    @Test
    fun `loads the sold plans, with the free trial and paid phase of each`() =
        runTest {
            connectAndLoadPlans()

            assertEquals(BillingState.PlansLoaded(OfferedPlans(yearlyPlan, monthlyPlan)), billing.state.value)
            coVerify { prefs.storePlans(listOf(yearlyPlan, monthlyPlan)) }
        }

    @Test
    fun `plans are unavailable when Play reports an error`() =
        runTest {
            play.connectsWith(BillingResponseCode.OK)
            billing.connect(activity) {}
            coEvery { prefs.subscriptions() } returns listOf(subscription)
            play.returnsProducts(code = BillingResponseCode.ERROR)

            billing.loadPlans()

            assertEquals(BillingState.PlansUnavailable, billing.state.value)
            coVerify(exactly = 0) { prefs.storePlans(any()) }
        }

    @Test
    fun `stores a completed purchase before reporting it`() =
        runTest {
            connectAndLoadPlans()

            billing.purchase("pia_yearly", activity)
            play.completesPurchase(BillingResponseCode.OK, purchase("pia_yearly", orderId = "order", token = "token"))

            coVerify { prefs.storePurchase(PurchaseData("token", "pia_yearly", "order")) }
            assertEquals(BillingState.Purchased, billing.state.value)
        }

    @Test
    fun `a purchase of another product or without an order fails`() =
        runTest {
            connectAndLoadPlans()

            billing.purchase("pia_yearly", activity)
            play.completesPurchase(BillingResponseCode.OK, purchase("pia_monthly"))
            assertTrue(billing.state.value is BillingState.PurchaseFailed)

            billing.purchase("pia_yearly", activity)
            play.completesPurchase(BillingResponseCode.OK, purchase("pia_yearly", orderId = null))
            assertTrue(billing.state.value is BillingState.PurchaseFailed)
            coVerify(exactly = 0) { prefs.storePurchase(any()) }
        }

    @Test
    fun `a Play error fails the purchase, but cancelling leaves the plans shown`() =
        runTest {
            connectAndLoadPlans()
            val plans = billing.state.value

            billing.purchase("pia_yearly", activity)
            play.completesPurchase(BillingResponseCode.USER_CANCELED)
            assertEquals(plans, billing.state.value)

            billing.purchase("pia_yearly", activity)
            play.completesPurchase(BillingResponseCode.ERROR)
            assertEquals(BillingState.PurchaseFailed("Billing failed: 6:debug"), billing.state.value)
        }

    @Test
    fun `ignores a second purchase while one is in progress`() =
        runTest {
            connectAndLoadPlans()

            billing.purchase("pia_yearly", activity)
            billing.purchase("pia_monthly", activity)

            verify(exactly = 1) { play.client.launchBillingFlow(any(), any()) }
        }

    @Test
    fun `purchase history returns the latest purchase of a sold product`() =
        runTest {
            connectAndLoadPlans()
            play.returnsPurchases(
                purchase("pia_monthly", token = "old", time = 1),
                purchase("unknown", token = "newest-unknown", time = 3),
                purchase("pia_yearly", token = "latest", time = 2),
            )

            billing.requestPurchaseHistory()

            assertEquals(PurchaseHistoryState.Success("latest", "pia_yearly"), billing.purchaseHistory.value)
        }

    @Test
    fun `purchase history fails when Play reports an error`() =
        runTest {
            connectAndLoadPlans()
            play.returnsPurchases(purchase("pia_yearly"), code = BillingResponseCode.ERROR)

            billing.requestPurchaseHistory()

            assertEquals(PurchaseHistoryState.Failed, billing.purchaseHistory.value)
        }

    @Test
    fun `only an auto-renewing, completed purchase counts as an active subscription`() =
        runTest {
            connectAndLoadPlans()

            play.returnsPurchases(
                purchase("pia_yearly", autoRenewing = false),
                purchase("pia_monthly", state = Purchase.PurchaseState.PENDING),
            )
            assertFalse(billing.hasActiveSubscription().first())

            play.returnsPurchases(purchase("pia_yearly"))
            assertTrue(billing.hasActiveSubscription().first())
        }

    @Test
    fun `reports the purchase token when Play says the subscription status changed`() =
        runTest {
            connectAndLoadPlans()

            fun respondWith(
                code: Int,
                token: String?,
            ) = every { play.client.showInAppMessages(any(), any(), any()) } answers {
                thirdArg<InAppMessageResponseListener>().onInAppMessageResponse(
                    mockk<InAppMessageResult> {
                        every { responseCode } returns code
                        every { purchaseToken } returns token
                    },
                )
                FakePlayBilling.result(BillingResponseCode.OK)
            }
            val updates = mutableListOf<String>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { billing.subscriptionStatusUpdates.collect(updates::add) }

            respondWith(InAppMessageResult.InAppMessageResponseCode.NO_ACTION_NEEDED, "ignored")
            billing.showPaymentIssueMessages(activity)
            respondWith(InAppMessageResult.InAppMessageResponseCode.SUBSCRIPTION_STATUS_UPDATED, "renewed")
            billing.showPaymentIssueMessages(activity)

            assertEquals(listOf("renewed"), updates)
        }
}