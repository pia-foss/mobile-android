package com.kape.payments.ui

import android.app.Activity
import com.amazon.device.drm.LicensingService
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.Product
import com.amazon.device.iap.model.ProductDataResponse
import com.amazon.device.iap.model.PurchaseResponse
import com.amazon.device.iap.model.PurchaseUpdatesResponse
import com.amazon.device.iap.model.UserDataResponse
import com.kape.payments.SubscriptionPrefs
import com.kape.payments.data.PurchaseData
import com.kape.payments.data.Subscription
import com.kape.payments.utils.PurchaseHistoryState
import com.kape.payments.utils.PurchaseState
import com.kape.payments.utils.monthlySubscription
import com.kape.payments.utils.yearlySubscription
import kotlinx.coroutines.flow.MutableStateFlow

private const val M1 = "PIA-M1"
private const val Y1 = "PIA-Y1"

class PaymentProviderImpl(private val prefs: SubscriptionPrefs, var activity: Activity? = null) :
    PaymentProvider {

    private val products = hashSetOf(monthlySubscription, yearlySubscription, M1, Y1)
    private var selectedProduct: Product? = null

    var availableProducts = mutableListOf<Product>()
    override val purchaseState: MutableStateFlow<PurchaseState> =
        MutableStateFlow(PurchaseState.Default)
    override val purchaseHistoryState =
        MutableStateFlow<PurchaseHistoryState>(PurchaseHistoryState.Default)

    override fun register(activity: Activity) {
        this.activity = activity
        LicensingService.verifyLicense(activity) {
            // currently do nothing
        }
        PurchasingService.registerListener(
            activity,
            object : PurchasingListener {
                override fun onUserDataResponse(userData: UserDataResponse?) {
                    // currently do nothing
                }

                override fun onProductDataResponse(productData: ProductDataResponse?) {
                    if (productData != null) {
                        val perMonth = productData.productData[M1]
                        val perYear = productData.productData[Y1]

                        if (perMonth != null && perYear != null) {
                            availableProducts.clear()
                            availableProducts.add(perMonth)
                            availableProducts.add(perYear)

                            val yearly = getYearlySubscription()
                            yearly.formattedPrice = perYear.price
                            val monthly = getMonthlySubscription()
                            monthly.formattedPrice = perMonth.price
                            prefs.storeSubscriptions(listOf(yearly, monthly))
                            purchaseState.value = PurchaseState.ProductsLoadedSuccess
                        } else {
                            purchaseState.value = PurchaseState.ProductsLoadedFailed
                        }
                    } else {
                        purchaseState.value = PurchaseState.ProductsLoadedFailed
                    }
                }

                override fun onPurchaseResponse(purchase: PurchaseResponse?) {
                    handlePurchaseResponse(purchase)
                }

                override fun onPurchaseUpdatesResponse(purchaseUpdate: PurchaseUpdatesResponse?) {
                    purchaseUpdate?.let {
                        prefs.storePurchaseData(
                            PurchaseData(
                                it.userData.userId,
                                it.receipts.first().receiptId,
                            ),
                        )
                        purchaseState.value = PurchaseState.PurchaseSuccess
                    }
                }
            },
        )
    }

    override fun getMonthlySubscription(): Subscription =
        prefs.getSubscriptions().first { plan -> plan.id == M1 }

    override fun getYearlySubscription(): Subscription =
        prefs.getSubscriptions().first { plan -> plan.id == Y1 }

    override fun loadProducts() {
        PurchasingService.getProductData(products)
    }

    override fun purchaseSelectedProduct(id: String) {
        selectedProduct = availableProducts.firstOrNull { it.sku == id }
        selectedProduct?.let {
            PurchasingService.purchase(id)
        }
    }

    override fun getPurchaseUpdates() {
        PurchasingService.getPurchaseUpdates(false)
    }

    override fun getPurchaseHistory() {
        // no-op
    }

    private fun handlePurchaseResponse(purchase: PurchaseResponse?) {
        if (purchase != null) {
            when (purchase.requestStatus) {
                PurchaseResponse.RequestStatus.SUCCESSFUL -> {
                    prefs.storePurchaseData(
                        PurchaseData(
                            purchase.userData.userId,
                            purchase.receipt.receiptId,
                        ),
                    )
                    purchaseState.value = PurchaseState.PurchaseSuccess
                }
                else -> purchaseState.value = PurchaseState.PurchaseFailed
            }
        } else {
            purchaseState.value = PurchaseState.PurchaseFailed
        }
    }
}