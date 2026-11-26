package com.kape.payments.data

import android.text.TextUtils
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic

// The billing classes build their own BillingClient, so the static builder is replaced to hand them this fake.
internal class FakePlayBilling {
    val client = mockk<BillingClient>(relaxed = true)
    private val listener = slot<PurchasesUpdatedListener>()

    init {
        val builder = mockk<BillingClient.Builder>()
        every { builder.setListener(capture(listener)) } returns builder
        every { builder.enablePendingPurchases(any()) } returns builder
        every { builder.build() } returns client
        mockkStatic(BillingClient::class)
        every { BillingClient.newBuilder(any()) } returns builder
        every { client.isReady } returns false
        // Play's purchase-screen builders call it, and the Android stubs in unit tests throw.
        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } answers { firstArg<CharSequence?>().isNullOrEmpty() }
    }

    fun close() = unmockkStatic(BillingClient::class, TextUtils::class)

    fun connectsWith(code: Int) {
        every { client.startConnection(any()) } answers {
            firstArg<BillingClientStateListener>().onBillingSetupFinished(result(code))
            every { client.isReady } returns (code == BillingClient.BillingResponseCode.OK)
        }
    }

    fun disconnects() {
        every { client.startConnection(any()) } answers { firstArg<BillingClientStateListener>().onBillingServiceDisconnected() }
    }

    fun returnsProducts(
        vararg products: ProductDetails,
        code: Int = BillingClient.BillingResponseCode.OK,
    ) {
        val details = mockk<QueryProductDetailsResult> { every { productDetailsList } returns products.toList() }
        every { client.queryProductDetailsAsync(any(), any()) } answers {
            secondArg<ProductDetailsResponseListener>().onProductDetailsResponse(result(code), details)
        }
    }

    fun returnsPurchases(
        vararg purchases: Purchase,
        code: Int = BillingClient.BillingResponseCode.OK,
    ) {
        every { client.queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } answers {
            secondArg<PurchasesResponseListener>().onQueryPurchasesResponse(result(code), purchases.toList())
        }
    }

    // What Play reports once the user finishes the purchase screen.
    fun completesPurchase(
        code: Int,
        vararg purchases: Purchase,
    ) = listener.captured.onPurchasesUpdated(result(code), purchases.toList())

    companion object {
        fun result(code: Int): BillingResult =
            BillingResult
                .newBuilder()
                .setResponseCode(code)
                .setDebugMessage("debug")
                .build()

        fun product(
            id: String,
            vararg phases: ProductDetails.PricingPhase,
        ): ProductDetails {
            val offer =
                mockk<ProductDetails.SubscriptionOfferDetails> {
                    every { offerToken } returns "offer-$id"
                    every { pricingPhases } returns mockk { every { pricingPhaseList } returns phases.toList() }
                }
            return mockk(relaxed = true) {
                every { productId } returns id
                every { productType } returns BillingClient.ProductType.SUBS
                every { subscriptionOfferDetails } returns if (phases.isEmpty()) null else listOf(offer)
            }
        }

        fun phase(
            micros: Long,
            period: String,
            price: String = "$${micros / 1_000_000.0}",
        ): ProductDetails.PricingPhase =
            mockk {
                every { priceAmountMicros } returns micros
                every { formattedPrice } returns price
                every { billingPeriod } returns period
                every { priceCurrencyCode } returns "USD"
            }

        fun purchase(
            productId: String,
            orderId: String? = "order-$productId",
            token: String = "token-$productId",
            acknowledged: Boolean = false,
            autoRenewing: Boolean = true,
            state: Int = Purchase.PurchaseState.PURCHASED,
            time: Long = 0,
        ): Purchase =
            mockk {
                every { products } returns listOf(productId)
                every { this@mockk.orderId } returns orderId
                every { purchaseToken } returns token
                every { isAcknowledged } returns acknowledged
                every { isAutoRenewing } returns autoRenewing
                every { purchaseState } returns state
                every { purchaseTime } returns time
            }
    }
}