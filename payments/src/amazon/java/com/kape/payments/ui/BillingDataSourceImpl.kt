package com.kape.payments.ui

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.amazon.device.drm.LicensingService
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import com.kape.payments.domain.BillingDataSource
import com.kape.payments.models.PurchaseData
import com.kape.payments.utils.PurchaseState
import com.kape.payments.utils.SubscriptionPrefs

private const val monthlySubscription = "MONTHLY"
private const val yearlySubscription = "YEARLY"
private const val monthlySubscriptionPeriod = "Monthly"
private const val yearlySubscriptionPeriod = "Annually"

class BillingDataSourceImpl(context: Context, private val prefs: SubscriptionPrefs, var activity: Activity? = null) :
    BillingDataSource {

    private val products = hashSetOf(monthlySubscription, yearlySubscription, "PIA-M1", "PIA-Y1")
    private var selectedProduct: Product? = null

    var availableProducts = mutableListOf<Product>()
    val purchaseState = mutableStateOf<PurchaseState>(PurchaseState.Default)

    init {
        LicensingService.verifyLicense(context) {
            // currently do nothing
        }
        PurchasingService.registerListener(context, object : PurchasingListener {
            override fun onUserDataResponse(userData: UserDataResponse?) {
                // currently do nothing
            }

            override fun onProductDataResponse(productData: ProductDataResponse?) {
                if (productData != null) {
                    val perMonth = productData.productData[monthlySubscription]
                    val perYear = productData.productData[yearlySubscription]

                    if (perMonth != null && perYear != null) {
                        availableProducts.clear()
                        availableProducts.add(perMonth)
                        availableProducts.add(perYear)
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
                    // TODO: handle purchase data in signup flow PurchaseData(it.userData.userId, it.receipts[0].receiptId)
                }
            }
        })
    }

    override fun loadProducts() {
        PurchasingService.getProductData(products)
    }

    override fun purchaseSelectedProduct(periodOrId: String) {
        selectedProduct = availableProducts.filter { it.subscriptionPeriod == periodOrId }[0]
        selectedProduct?.let {
            when (it.subscriptionPeriod) {
                monthlySubscriptionPeriod -> PurchasingService.purchase(monthlySubscription)
                yearlySubscriptionPeriod -> PurchasingService.purchase(yearlySubscription)
                else -> {
                    // do nothing
                }
            }
        }
    }

    private fun handlePurchaseResponse(purchase: PurchaseResponse?) {
        if (purchase != null) {
            when (purchase.requestStatus) {
                PurchaseResponse.RequestStatus.SUCCESSFUL -> {
                    prefs.storePurchaseData(
                        PurchaseData(
                            purchase.userData.userId,
                            purchase.receipt.receiptId
                        )
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