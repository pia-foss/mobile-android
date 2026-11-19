package com.kape.payments.data

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.kape.data.model.DipPurchaseData
import com.kape.payments.data.FakePlayBilling.Companion.phase
import com.kape.payments.data.FakePlayBilling.Companion.product
import com.kape.payments.data.FakePlayBilling.Companion.purchase
import com.kape.payments.domain.BillingPeriod
import com.kape.payments.domain.ProductPrice
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PlayStoreDipSubscriptionBillingTest {
    private val play = FakePlayBilling()
    private val context = mockk<Context> { every { applicationContext } returns this@mockk }
    private val billing = PlayStoreDipSubscriptionBilling(context, mockk(relaxed = true))
    private val activity = mockk<Activity>()

    private val yearly = product("dip_yearly", phase(99_990_000, BillingPeriod.YEARLY, "$99.99"))

    @AfterEach
    fun tearDown() = play.close()

    @Test
    fun `nothing is queried when Play billing can't connect`() =
        runTest {
            play.connectsWith(BillingResponseCode.BILLING_UNAVAILABLE)

            assertNull(billing.prices(listOf("dip_yearly")))
            assertNull(billing.unacknowledgedProducts(listOf("dip_yearly")))
            assertTrue(billing.purchase(activity, "dip_yearly").isFailure)
            verify(exactly = 0) { play.client.queryProductDetailsAsync(any(), any()) }
        }

    @Test
    fun `a lost connection during setup counts as unavailable`() =
        runTest {
            play.disconnects()

            assertNull(billing.prices(listOf("dip_yearly")))
        }

    @Test
    fun `prices come from each product's first pricing phase, skipping products without offers`() =
        runTest {
            play.connectsWith(BillingResponseCode.OK)
            play.returnsProducts(yearly, product("dip_monthly"))

            assertEquals(
                listOf(ProductPrice("dip_yearly", "$99.99", 99_990_000, "USD")),
                billing.prices(listOf("dip_yearly", "dip_monthly")),
            )
        }

    @Test
    fun `prices fail when Play reports an error`() =
        runTest {
            play.connectsWith(BillingResponseCode.OK)
            play.returnsProducts(yearly, code = BillingResponseCode.SERVICE_UNAVAILABLE)

            assertNull(billing.prices(listOf("dip_yearly")))
        }

    @Test
    fun `lists only unacknowledged purchases of the requested products`() =
        runTest {
            play.connectsWith(BillingResponseCode.OK)
            play.returnsPurchases(
                purchase("dip_yearly"),
                purchase("dip_monthly", acknowledged = true),
                purchase("pia_vpn_yearly"),
            )

            assertEquals(listOf("dip_yearly"), billing.unacknowledgedProducts(listOf("dip_yearly", "dip_monthly")))
        }

    @Test
    fun `unacknowledged purchases fail when Play reports an error`() =
        runTest {
            play.connectsWith(BillingResponseCode.OK)
            play.returnsPurchases(code = BillingResponseCode.ERROR)

            assertNull(billing.unacknowledgedProducts(listOf("dip_yearly")))
        }

    @Test
    fun `can't buy a product whose price was never loaded`() =
        runTest {
            play.connectsWith(BillingResponseCode.OK)

            assertTrue(billing.purchase(activity, "dip_yearly").isFailure)
            verify(exactly = 0) { play.client.launchBillingFlow(any(), any()) }
        }

    @Test
    fun `a completed purchase returns its receipt`() =
        runTest {
            loadYearlyPrice()

            val result = async { billing.purchase(activity, "dip_yearly") }
            runCurrent()
            play.completesPurchase(BillingResponseCode.OK, purchase("dip_yearly", orderId = "order", token = "token"))

            assertEquals(DipPurchaseData(token = "token", productId = "dip_yearly", orderId = "order"), result.await().getOrNull())
        }

    @Test
    fun `cancelling, failing or buying something else fails the purchase`() =
        runTest {
            loadYearlyPrice()

            assertPurchaseFails { play.completesPurchase(BillingResponseCode.USER_CANCELED) }
            assertPurchaseFails { play.completesPurchase(BillingResponseCode.ERROR) }
            assertPurchaseFails { play.completesPurchase(BillingResponseCode.OK, purchase("other_product")) }
            assertPurchaseFails { play.completesPurchase(BillingResponseCode.OK, purchase("dip_yearly", orderId = null)) }
        }

    private suspend fun loadYearlyPrice() {
        play.connectsWith(BillingResponseCode.OK)
        play.returnsProducts(yearly)
        billing.prices(listOf("dip_yearly"))
    }

    private suspend fun TestScope.assertPurchaseFails(complete: () -> Unit) {
        val result = async { billing.purchase(activity, "dip_yearly") }
        runCurrent()
        complete()
        assertTrue(result.await().isFailure)
    }
}