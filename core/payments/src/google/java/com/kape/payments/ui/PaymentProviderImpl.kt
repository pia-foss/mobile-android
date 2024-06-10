package com.kape.payments.ui

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.PurchaseHistoryResponseListener
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.QueryPurchasesParams
import com.kape.payments.SubscriptionPrefs
import com.kape.payments.data.PurchaseData
import com.kape.payments.data.Subscription
import com.kape.payments.utils.PurchaseHistoryState
import com.kape.payments.utils.PurchaseState
import com.kape.payments.utils.monthlySubscription
import com.kape.payments.utils.yearlySubscription
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class PaymentProviderImpl(private val prefs: SubscriptionPrefs, var activity: Activity? = null) :
    PaymentProvider {

    private lateinit var billingClient: BillingClient
    private var selectedProduct: ProductDetails? = null

    private val availableProducts = mutableListOf<ProductDetails>()

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (purchases != null) {
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        val purchase = purchases.first()
                        purchase.orderId?.let {
                            prefs.storeVpnPurchaseData(
                                PurchaseData(
                                    purchase.purchaseToken,
                                    purchase.products.first(),
                                    it,
                                ),
                            )
                        }
                        purchaseState.value = PurchaseState.PurchaseSuccess
                    }

                    else -> purchaseState.value = PurchaseState.PurchaseFailed
                }
            }
        }

    override val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Default)
    override val purchaseHistoryState =
        MutableStateFlow<PurchaseHistoryState>(PurchaseHistoryState.Default)

    override fun register(activity: Activity) {
        this.activity = activity
        billingClient = BillingClient.newBuilder(activity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        purchaseState.value = PurchaseState.InitSuccess
                    } else {
                        purchaseState.value = PurchaseState.InitFailed
                    }
                }

                override fun onBillingServiceDisconnected() {
                    purchaseState.value = PurchaseState.Disconnected
                }
            },
        )
    }

    override fun getMonthlySubscription(): Subscription = prefs.getVpnSubscriptions().first {
        it.plan.lowercase() == monthlySubscription.lowercase()
    }

    override fun getYearlySubscription(): Subscription = prefs.getVpnSubscriptions().first {
        it.plan.lowercase() == yearlySubscription.lowercase()
    }

    override fun loadProducts() {
        if (prefs.getVpnSubscriptions().isEmpty()) {
            purchaseState.value = PurchaseState.ProductsLoadedFailed
        } else {
            loadProviderProducts()
        }
    }

    private fun loadProviderProducts() {
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(createProductsListForQuery())
                .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) {
                billingResult,
                productDetailsList,
            ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val data = prefs.getVpnSubscriptions()
                for (item in productDetailsList) {
                    if (data.any { it.id == item.productId }) {
                        item.subscriptionOfferDetails?.let {
                            // if offers are more than one - there's a free trial
                            if (it.size > 1) {
                                data.first { it.id == item.productId }.formattedPrice =
                                    item.subscriptionOfferDetails?.getOrNull(1)?.pricingPhases?.pricingPhaseList?.get(
                                        0,
                                    )?.formattedPrice
                            } else {
                                data.first { it.id == item.productId }.formattedPrice =
                                    item.subscriptionOfferDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.get(
                                        0,
                                    )?.formattedPrice
                            }
                        }
                    }
                }
                prefs.storeVpnSubscriptions(data)
                availableProducts.clear()
                availableProducts.addAll(productDetailsList)
                purchaseState.value = PurchaseState.ProductsLoadedSuccess
            } else {
                purchaseState.value = PurchaseState.ProductsLoadedFailed
            }
        }
    }

    override fun purchaseSelectedProduct(id: String) {
        selectedProduct = availableProducts.first { it.productId == id }
        activity?.let { currentActivity ->
            selectedProduct?.let { productId ->
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(createProductQuery(productId))
                    .build()

                billingClient.launchBillingFlow(currentActivity, billingFlowParams)
            }
        }
    }

    override fun getPurchaseUpdates() {
        // no-op
    }

    override fun getPurchaseHistory() {
        val params =
            QueryPurchaseHistoryParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                .build()

        val purchaseHistoryListener = object : PurchaseHistoryResponseListener {
            override fun onPurchaseHistoryResponse(
                billingResponse: BillingResult,
                purchases: MutableList<PurchaseHistoryRecord>?,
            ) {
                if (billingResponse.responseCode != BillingClient.BillingResponseCode.OK) {
                    purchaseHistoryState.value = PurchaseHistoryState.PurchaseHistoryFailed
                    return
                }
                if (purchases.isNullOrEmpty()) {
                    purchaseHistoryState.value = PurchaseHistoryState.PurchaseHistoryFailed
                    return
                }

                for (p: PurchaseHistoryRecord in purchases.sortedByDescending { it.purchaseTime }) {
                    if (availableProducts.any { it.productId == p.products[0] }) {
                        purchaseHistoryState.value =
                            PurchaseHistoryState.PurchaseHistorySuccess(
                                p.purchaseToken,
                                p.products[0],
                            )
                        break
                    }
                }
            }
        }
        billingClient.queryPurchaseHistoryAsync(params, purchaseHistoryListener)
    }

    override fun hasActiveSubscription(): Flow<Boolean> = callbackFlow {
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        val purchasesListener = PurchasesResponseListener { billingResult: BillingResult, purchases: List<Purchase> ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                trySend(false)
                return@PurchasesResponseListener
            }

            if (purchases.isEmpty()) {
                trySend(false)
                return@PurchasesResponseListener
            }

            val hasActiveSubscription = purchases.any { purchase ->
                purchase.isAutoRenewing && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            }
            trySend(hasActiveSubscription)
        }
        billingClient.queryPurchasesAsync(params, purchasesListener)
        awaitClose { channel.close() }
    }

    override fun isClientRegistered(): Boolean {
        return billingClient.isReady
    }

    private fun createProductsListForQuery(): List<QueryProductDetailsParams.Product> {
        val result = mutableListOf<QueryProductDetailsParams.Product>()
        for (product in prefs.getVpnSubscriptions()) {
            result.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product.id)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
            )
        }
        return result
    }

    private fun createProductQuery(product: ProductDetails): List<BillingFlowParams.ProductDetailsParams> {
        val result = mutableListOf<BillingFlowParams.ProductDetailsParams>()
        val offerToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
        result.add(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product)
                .setOfferToken(offerToken)
                .build(),
        )
        return result
    }
}