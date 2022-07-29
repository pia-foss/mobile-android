package com.kape.payments.ui

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.kape.payments.domain.BillingDataSource
import com.kape.payments.models.Subscription
import com.kape.payments.utils.PurchaseState
import com.kape.payments.utils.SubscriptionPrefs
import com.kape.payments.utils.monthlySubscription
import com.kape.payments.utils.yearlySubscription
import kotlinx.coroutines.flow.MutableStateFlow

class BillingDataSourceImpl(context: Context, private val prefs: SubscriptionPrefs, var activity: Activity? = null) : BillingDataSource {

    private var billingClient: BillingClient
    private var selectedProduct: ProductDetails? = null

    private val availableProducts = mutableListOf<ProductDetails>()

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (purchases != null) {
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK ->
                        purchaseState.value = PurchaseState.PurchaseSuccess
                    else -> purchaseState.value = PurchaseState.PurchaseFailed
                }
            }
        }

    init {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // TODO: handle success
                    purchaseState.value = PurchaseState.InitSuccess
                } else {
                    // TODO: handle error
                    purchaseState.value = PurchaseState.InitFailed
                }
            }

            override fun onBillingServiceDisconnected() {
                // TODO: handle error
                purchaseState.value = PurchaseState.InitFailed
            }
        })
    }

    override val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Default)
    override fun getMonthlySubscription(): Subscription = prefs.getSubscriptions().first {
        it.plan.lowercase() == monthlySubscription.lowercase()
    }

    override fun getYearlySubscription(): Subscription = prefs.getSubscriptions().first {
        it.plan.lowercase() == yearlySubscription.lowercase()
    }

    override fun loadProducts() {
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(createProductsListForQuery())
                .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,
                                                                            productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val data = prefs.getSubscriptions()
                for (item in productDetailsList) {
                    if (data.any { it.id == item.productId }) {
                        data.first { it.id == item.productId }.formattedPrice =
                            item.subscriptionOfferDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice
                    }
                }
                prefs.storeSubscriptions(data)
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

    private fun createProductsListForQuery(): List<QueryProductDetailsParams.Product> {
        val result = mutableListOf<QueryProductDetailsParams.Product>()
        for (product in prefs.getSubscriptions()) {
            result.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product.id)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
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
                .build()
        )
        return result
    }
}