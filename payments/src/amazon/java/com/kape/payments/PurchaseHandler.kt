package com.kape.payments

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.amazon.device.drm.LicensingService
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.ProductDataResponse
import com.amazon.device.iap.model.PurchaseResponse
import com.amazon.device.iap.model.PurchaseUpdatesResponse
import com.amazon.device.iap.model.UserDataResponse
import com.kape.payments.models.Price
import com.kape.payments.models.PurchaseData
import com.kape.payments.utils.PurchaseUtil
import com.kape.payments.utils.SubscriptionPrefs
import com.kape.payments.utils.monthlyPlan
import com.kape.payments.utils.yearlyPlan

class PurchaseHandler(private val prefs: SubscriptionPrefs) : PurchaseUtil {

    private val products = hashSetOf(monthlyPlan, yearlyPlan, "PIA-M1", "PIA-Y1")
    private var selectedProduct: String? = null

    val subscriptions = mutableStateOf<Price?>(null)
    val purchaseData = mutableStateOf<PurchaseData?>(null)
    val purchaseState = mutableStateOf<PurchaseState?>(null)

    override fun init(context: Context) {
        LicensingService.verifyLicense(context) {
            // currently do nothing
        }
        PurchasingService.registerListener(context, object : PurchasingListener {
            override fun onUserDataResponse(userData: UserDataResponse?) {
                // currently do nothing
            }

            override fun onProductDataResponse(productData: ProductDataResponse?) {
                if (productData != null) {
                    if (productData.productData.containsKey(monthlyPlan) && productData.productData.containsKey(yearlyPlan)) {
                        subscriptions.value = Price(
                            productData.productData.getValue(monthlyPlan).price, productData.productData.getValue(
                                yearlyPlan
                            ).price
                        )
                    } else {
                        Log.d("amazon purchase handler", "products are missing")
                    }
                } else {
                    Log.d("amazon purchase handler", "productData is missing")
                }
            }

            override fun onPurchaseResponse(purchase: PurchaseResponse?) {
                handlePurchaseResponse(purchase)
            }

            override fun onPurchaseUpdatesResponse(purchaseUpdate: PurchaseUpdatesResponse?) {
                purchaseUpdate?.let {
                    purchaseData.value = PurchaseData(it.userData.userId, it.receipts[0].receiptId)
                }
            }
        })
    }

    override fun loadProducts() {
        PurchasingService.getProductData(products)
    }

    override fun purchaseSelectedProduct() {
        PurchasingService.purchase(selectedProduct)
    }

    override fun selectProduct(isYearly: Boolean) {
        selectedProduct = if (isYearly) {
            yearlyPlan
        } else {
            monthlyPlan
        }
    }

    override fun getSelectedProductId(): String? {
        return if (prefs.getSubscriptions().isEmpty()) {
            null
        } else {
            prefs.getSubscriptions().filter { it.plan == selectedProduct?.toLowerCase(Locale.current) }.getOrNull(0)?.id
        }
    }

    override fun getPurchaseUpdates() {
        PurchasingService.getPurchaseUpdates(false)
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
                    purchaseState.value = PurchaseState.Successful
                }
                PurchaseResponse.RequestStatus.ALREADY_PURCHASED -> {
                    purchaseState.value = PurchaseState.AlreadyPurchased
                }
                PurchaseResponse.RequestStatus.FAILED -> {
                    purchaseState.value = PurchaseState.Failed
                }
                PurchaseResponse.RequestStatus.INVALID_SKU -> {
                    purchaseState.value = PurchaseState.InvalidSku
                }
                PurchaseResponse.RequestStatus.NOT_SUPPORTED -> {
                    purchaseState.value = PurchaseState.NotSupported
                }

                else -> {
                    // do nothing
                }
            }
        } else {
            purchaseState.value = PurchaseState.Error
        }
    }

    sealed class PurchaseState {
        object Successful : PurchaseState()
        object AlreadyPurchased : PurchaseState()
        object Failed : PurchaseState()
        object InvalidSku : PurchaseState()
        object NotSupported : PurchaseState()
        object Error : PurchaseState()
    }
}