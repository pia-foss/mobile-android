package com.kape.payments.ui

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.kape.payments.SubscriptionPrefs
import com.kape.payments.data.PurchaseData
import com.kape.payments.data.Subscription
import com.kape.payments.data.SubscriptionPlan
import com.kape.payments.utils.MONTHLY
import com.kape.payments.utils.PurchaseHistoryState
import com.kape.payments.utils.PurchaseState
import com.kape.payments.utils.YEARLY
import com.kape.payments.utils.monthlySubscription
import com.kape.payments.utils.yearlySubscription
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class VpnSubscriptionPaymentProviderImpl(
    private val prefs: SubscriptionPrefs,
    private var activity: Activity? = null,
) : VpnSubscriptionPaymentProvider {

    private lateinit var billingClient: BillingClient
    private var selectedProduct: ProductDetails? = null

    private val availableProducts = mutableListOf<ProductDetails>()

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (purchases != null) {
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        val purchase = purchases.first()
                        purchase.products.firstOrNull {
                            it == selectedProduct?.productId
                        }?.let { productId ->
                            purchase.orderId?.let { orderId ->
                                prefs.storeVpnPurchaseData(
                                    PurchaseData(
                                        purchase.purchaseToken,
                                        productId,
                                        orderId,
                                    ),
                                )
                            }
                            purchaseState.value = PurchaseState.PurchaseSuccess
                        }
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
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
            )
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

    @Deprecated("Deprecated in favor of SubscriptionPlan")
    override fun getMonthlySubscription(): Subscription? = prefs.getVpnSubscriptions().firstOrNull {
        it.plan.equals(monthlySubscription, ignoreCase = true)
    }

    @Deprecated("Deprecated in favor of SubscriptionPlan")
    override fun getYearlySubscription(): Subscription? = prefs.getVpnSubscriptions().firstOrNull {
        it.plan.equals(yearlySubscription, ignoreCase = true)
    }

    override fun getMonthlySubscriptionPlan(): SubscriptionPlan? {
        return prefs.getVpnSubscriptionPlans().firstOrNull {
            it.billingPeriod == MONTHLY
        }
    }

    override fun getYearlySubscriptionPlan(): SubscriptionPlan? {
        return prefs.getVpnSubscriptionPlans().firstOrNull {
            it.billingPeriod == YEARLY
        }
    }

    override fun getFreeTrialYearlySubscriptionPlan(): SubscriptionPlan? {
        return prefs.getVpnSubscriptionPlans().firstOrNull {
            it.billingPeriod == YEARLY && it.freeTrialDuration != null
        }
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
                val plans = mutableListOf<SubscriptionPlan>()
                for (item in productDetailsList.productDetailsList) {
                    if (data.any { it.id == item.productId }) {
                        item.subscriptionOfferDetails?.let { subOfferDetails ->
                            subOfferDetails.forEach { details ->
                                val freeTrial =
                                    details.pricingPhases.pricingPhaseList.firstOrNull { it.priceAmountMicros == 0L }
                                val plan =
                                    details.pricingPhases.pricingPhaseList.first { it.priceAmountMicros != 0L }
                                val planPeriod = when (plan.billingPeriod) {
                                    MONTHLY -> monthlySubscription
                                    YEARLY -> yearlySubscription
                                    else -> ""
                                }
                                val subscriptionPlan = SubscriptionPlan(
                                    id = item.productId,
                                    billingPeriod = plan.billingPeriod,
                                    priceInMicros = plan.priceAmountMicros,
                                    formattedPrice = plan.formattedPrice,
                                    freeTrialDuration = freeTrial?.billingPeriod,
                                    plan = planPeriod,
                                )
                                if (!plans.contains(subscriptionPlan)) {
                                    plans.add(subscriptionPlan)
                                }
                            }
                        }
                    }
                }
                prefs.storeVpnSubscriptions(data)
                prefs.storeVpnSubscriptionPlans(plans)
                availableProducts.clear()
                availableProducts.addAll(productDetailsList.productDetailsList)
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
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                .build()

        val purchaseHistoryListener = object : PurchasesResponseListener {
            override fun onQueryPurchasesResponse(
                billingResponse: BillingResult,
                purchases: MutableList<Purchase>,
            ) {
                if (billingResponse.responseCode != BillingClient.BillingResponseCode.OK) {
                    purchaseHistoryState.value = PurchaseHistoryState.PurchaseHistoryFailed
                    return
                }
                if (purchases.isEmpty()) {
                    purchaseHistoryState.value = PurchaseHistoryState.PurchaseHistoryFailed
                    return
                }

                for (p: Purchase in purchases.sortedByDescending { it.purchaseTime }) {
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
        billingClient.queryPurchasesAsync(params, purchaseHistoryListener)
    }

    override fun hasActiveSubscription(): Flow<Boolean> = callbackFlow {
        val params =
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        val purchasesListener =
            PurchasesResponseListener { billingResult: BillingResult, purchases: List<Purchase> ->
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

    override fun reset() {
        purchaseState.value = PurchaseState.Default
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