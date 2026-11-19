package com.kape.payments.data

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.kape.data.model.DipPurchaseData
import com.kape.payments.domain.DipSubscriptionBilling
import com.kape.payments.domain.ProductPrice
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton(binds = [DipSubscriptionBilling::class])
internal class PlayStoreDipSubscriptionBilling(
    context: Context,
    private val prefs: SubscriptionPrefs,
) : DipSubscriptionBilling {
    private val products = mutableListOf<ProductDetails>()
    private var purchaseResult: CompletableDeferred<Result<DipPurchaseData>>? = null

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { result, purchases ->
            val outcome =
                when {
                    result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED ->
                        Result.failure(IllegalStateException("Purchase cancelled by user"))
                    result.responseCode != BillingClient.BillingResponseCode.OK || purchases == null ->
                        Result.failure(IllegalStateException("Billing failed: ${result.responseCode}:${result.debugMessage}"))
                    else -> {
                        val knownIds = products.map { it.productId }.toSet()
                        val purchase = purchases.firstOrNull { it.products.any(knownIds::contains) }
                        val productId = purchase?.products?.first(knownIds::contains)
                        val orderId = purchase?.orderId
                        if (purchase != null && productId != null && orderId != null) {
                            Result.success(DipPurchaseData(token = purchase.purchaseToken, productId = productId, orderId = orderId))
                        } else {
                            Result.failure(IllegalStateException("Unknown product purchase"))
                        }
                    }
                }
            purchaseResult?.complete(outcome)
        }

    private val client: BillingClient =
        BillingClient
            .newBuilder(context.applicationContext)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()

    override suspend fun prices(productIds: List<String>): List<ProductPrice>? {
        if (!connect()) return null
        val productList =
            productIds.map {
                QueryProductDetailsParams.Product
                    .newBuilder()
                    .setProductId(it)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }
        val (result, details) =
            suspendCancellableCoroutine { continuation ->
                client.queryProductDetailsAsync(
                    QueryProductDetailsParams.newBuilder().setProductList(productList).build(),
                ) { result, details ->
                    continuation.resume(result to details.productDetailsList)
                }
            }
        if (result.responseCode != BillingClient.BillingResponseCode.OK) return null
        details.filterNot { detail -> products.any { it.productId == detail.productId } }.let(products::addAll)
        return details.mapNotNull { product ->
            product.subscriptionOfferDetails
                ?.firstOrNull()
                ?.pricingPhases
                ?.pricingPhaseList
                ?.firstOrNull()
                ?.let { phase -> ProductPrice(product.productId, phase.formattedPrice, phase.priceAmountMicros, phase.priceCurrencyCode) }
        }
    }

    override suspend fun unacknowledgedProducts(productIds: List<String>): List<String>? {
        if (!connect()) return null
        return suspendCancellableCoroutine { continuation ->
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
            ) { result, purchases ->
                continuation.resume(
                    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                        null
                    } else {
                        purchases.filterNot { it.isAcknowledged }.flatMap { it.products }.filter(productIds::contains)
                    },
                )
            }
        }
    }

    override suspend fun purchase(
        activity: Activity,
        productId: String,
    ): Result<DipPurchaseData> {
        if (!connect()) return Result.failure(IllegalStateException("Billing unavailable"))
        val product =
            products.firstOrNull { it.productId == productId } ?: return Result.failure(IllegalStateException("Invalid productId"))
        val pending = CompletableDeferred<Result<DipPurchaseData>>()
        purchaseResult = pending
        val params =
            BillingFlowParams.ProductDetailsParams
                .newBuilder()
                .setProductDetails(product)
                .setOfferToken(
                    product.subscriptionOfferDetails
                        ?.firstOrNull()
                        ?.offerToken
                        .orEmpty(),
                ).build()
        client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(params)).build())
        return pending.await().also { purchaseResult = null }
    }

    override suspend fun pendingPurchase(): DipPurchaseData? = prefs.dipPurchase()

    override suspend fun storePendingPurchase(purchase: DipPurchaseData) = prefs.storeDipPurchase(purchase)

    override suspend fun clearPendingPurchase() = prefs.removeDipPurchase()

    private suspend fun connect(): Boolean {
        if (client.isReady) return true
        return suspendCancellableCoroutine { continuation ->
            client.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        if (continuation.isActive) continuation.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                    }

                    override fun onBillingServiceDisconnected() {
                        if (continuation.isActive) continuation.resume(false)
                    }
                },
            )
        }
    }
}