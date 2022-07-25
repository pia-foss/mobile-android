package com.kape.payments.ui

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.android.billingclient.api.*
import com.kape.payments.domain.BillingDataSource
import com.kape.payments.utils.PurchaseState
import com.kape.payments.utils.SubscriptionPrefs

class BillingDataSourceImpl(context: Context, private val prefs: SubscriptionPrefs, var activity: Activity? = null) : BillingDataSource {

    private var billingClient: BillingClient
    private var selectedProduct: ProductDetails? = null

    private val availableProducts = mutableListOf<ProductDetails>()
    val purchaseState = mutableStateOf<PurchaseState>(PurchaseState.Default)

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (purchases != null) {
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK ->
                        purchaseState.value = PurchaseState.PurchaseSuccess
                    else ->
                        purchaseState.value = PurchaseState.PurchaseFailed
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
                    purchaseState.value = PurchaseState.InitSuccess
                } else {
                    purchaseState.value = PurchaseState.InitFailed
                }
            }

            override fun onBillingServiceDisconnected() {
                purchaseState.value = PurchaseState.InitFailed
            }
        })
    }

    override fun loadProducts() {
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(createProductsListForQuery())
                .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,
                                                                            productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                availableProducts.clear()
                availableProducts.addAll(productDetailsList)
                purchaseState.value = PurchaseState.ProductsLoadedSuccess
            } else {
                purchaseState.value = PurchaseState.ProductsLoadedFailed
            }
        }
    }

    override fun purchaseSelectedProduct(periodOrId: String) {
        selectedProduct = availableProducts.filter { it.productId == periodOrId }[0]
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
        val offerToken = product.subscriptionOfferDetails?.get(0)?.offerToken ?: ""
        result.add(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product)
                .setOfferToken(offerToken)
                .build()
        )
        return result
    }

}