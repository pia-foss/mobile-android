package com.kape.payments.ui

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.kape.payments.data.DipPurchaseData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class DipSubscriptionPaymentProviderImpl(
    private val context: Context,
) : DipSubscriptionPaymentProvider, CoroutineScope {

    private val availableProducts: MutableList<ProductDetails> = mutableListOf()
    private var purchaseCompletableDeferred: CompletableDeferred<Result<DipPurchaseData>>? = null
    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (purchases == null || result.responseCode != BillingClient.BillingResponseCode.OK) {
            purchaseCompletableDeferred?.complete(Result.failure(IllegalStateException("Billing failed")))
            return@PurchasesUpdatedListener
        }

        val knownPurchase = purchases.firstOrNull {
            it.products.any { productId ->
                availableProducts.any { knownProductDetails ->
                    productId == knownProductDetails.productId
                }
            }
        }
        val orderId = knownPurchase?.orderId

        if (knownPurchase != null && orderId != null) {
            val knownProduct = knownPurchase.products.first { productId ->
                availableProducts.any { knownProductDetails ->
                    productId == knownProductDetails.productId
                }
            }

            purchaseCompletableDeferred?.complete(
                Result.success(
                    DipPurchaseData(
                        token = knownPurchase.purchaseToken,
                        productId = knownProduct,
                        orderId = orderId,
                    ),
                ),
            )
        } else {
            purchaseCompletableDeferred?.complete(
                Result.failure(IllegalStateException("Unknown product purchase")),
            )
        }
    }
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        launch {
            connectBilling()
        }
    }

    // region CoroutineScope
    override val coroutineContext: CoroutineContext
        get() = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    // endregion

    // region DipSubscriptionPaymentProvider
    override fun productsDetails(
        productIds: List<String>,
        callback: (result: Result<List<Pair<String, String>>>) -> Unit,
    ) {
        launch {
            if (billingClient.isReady.not()) {
                connectBilling()
            }

            val productList = mutableListOf<QueryProductDetailsParams.Product>()
            for (productId in productIds) {
                productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                )
            }

            val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
            ) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val result = mutableListOf<Pair<String, String>>()
                    for (product in productDetailsList) {
                        if (availableProducts.any { it.productId == product.productId }.not()) {
                            availableProducts.add(product)
                        }

                        product.subscriptionOfferDetails?.let {
                            result.add(
                                Pair(
                                    product.productId,
                                    it.last().pricingPhases.pricingPhaseList.first().formattedPrice,
                                ),
                            )
                        }
                    }
                    callback(Result.success(result))
                } else {
                    callback(Result.failure(IllegalStateException("Failed to query products")))
                }
            }
        }
    }

    override fun unacknowledgedProductIds(
        productIds: List<String>,
        callback: (result: Result<List<String>>) -> Unit,
    ) {
        launch {
            if (billingClient.isReady.not()) {
                connectBilling()
            }

            val queryPurchasesParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            billingClient.queryPurchasesAsync(queryPurchasesParams) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val result = mutableListOf<String>()
                    val unacknowledgedPurchases = purchases.filter { it.isAcknowledged.not() }
                    for (unacknowledgedPurchase in unacknowledgedPurchases) {
                        for (product in unacknowledgedPurchase.products) {
                            if (productIds.contains(product)) {
                                result.add(product)
                            }
                        }
                    }
                    callback(Result.success(result))
                } else {
                    callback(Result.failure(IllegalStateException("Failed to query purchases")))
                }
            }
        }
    }

    override fun purchaseProduct(
        activity: Activity,
        productId: String,
        callback: (result: Result<DipPurchaseData>) -> Unit,
    ) {
        launch {
            if (billingClient.isReady.not()) {
                connectBilling()
            }

            val productDetails = availableProducts.firstOrNull { it.productId == productId }
            if (productDetails == null) {
                callback(Result.failure(IllegalStateException("Invalid productId")))
                return@launch
            }

            purchaseCompletableDeferred = CompletableDeferred()
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
            val productList: List<BillingFlowParams.ProductDetailsParams> = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build(),
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productList)
                .build()

            billingClient.launchBillingFlow(activity, billingFlowParams)

            val result = purchaseCompletableDeferred!!.await()
            purchaseCompletableDeferred = null
            callback(result)
        }
    }
    // endregion

    // region private
    private suspend fun connectBilling(): Result<Unit> {
        val deferred: CompletableDeferred<Result<Unit>> = CompletableDeferred()
        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        deferred.complete(Result.success(Unit))
                    } else {
                        deferred.complete(Result.failure(IllegalStateException("Billing setup failed")))
                    }
                }
                override fun onBillingServiceDisconnected() {
                    deferred.complete(Result.failure(IllegalStateException("Billing disconnected")))
                }
            },
        )
        return deferred.await()
    }
    // endregion
}