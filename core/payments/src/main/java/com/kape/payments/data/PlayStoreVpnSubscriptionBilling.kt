package com.kape.payments.data

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.InAppMessageParams
import com.android.billingclient.api.InAppMessageResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.kape.contracts.PurchaseHistoryState
import com.kape.contracts.di.Qualifiers
import com.kape.data.model.PurchaseData
import com.kape.payments.domain.BillingPeriod
import com.kape.payments.domain.BillingState
import com.kape.payments.domain.SubscriptionPlan
import com.kape.payments.domain.VpnSubscriptionBilling
import com.kape.payments.domain.offeredPlans
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

private const val MONTHLY_PLAN = "MONTHLY"
private const val YEARLY_PLAN = "YEARLY"

@Singleton(binds = [VpnSubscriptionBilling::class])
internal class PlayStoreVpnSubscriptionBilling(
    private val prefs: SubscriptionPrefs,
    private val subscriptions: SubscriptionsDataSource,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) : VpnSubscriptionBilling {
    private var billingClient: BillingClient? = null
    private val products = mutableListOf<ProductDetails>()
    private var selectedProduct: ProductDetails? = null
    private var purchaseInProgress = false

    private val _state = MutableStateFlow<BillingState>(BillingState.NotConnected)
    override val state: StateFlow<BillingState> = _state.asStateFlow()
    private val _purchaseHistory = MutableStateFlow<PurchaseHistoryState>(PurchaseHistoryState.Default)
    override val purchaseHistory: StateFlow<PurchaseHistoryState> = _purchaseHistory.asStateFlow()
    private val _subscriptionStatusUpdates = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val subscriptionStatusUpdates: Flow<String> = _subscriptionStatusUpdates.asSharedFlow()

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { result, purchases ->
            purchaseInProgress = false
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK -> onPurchased(purchases?.firstOrNull())
                BillingClient.BillingResponseCode.USER_CANCELED -> Unit
                else -> _state.value = BillingState.PurchaseFailed("Billing failed: ${result.responseCode}:${result.debugMessage}")
            }
        }

    override fun connect(
        activity: Activity,
        onReady: () -> Unit,
    ) {
        val client =
            BillingClient
                .newBuilder(activity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build()
        billingClient = client
        client.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        _state.value = BillingState.Ready
                        onReady()
                    } else {
                        _state.value = BillingState.Unavailable
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _state.value = BillingState.Disconnected
                }
            },
        )
    }

    override fun isConnected(): Boolean = billingClient?.isReady == true

    override suspend fun loadPlans() {
        subscriptions.fetchSubscriptions()?.let { prefs.storeSubscriptions(it) }
        val sold = prefs.subscriptions()
        val client = billingClient
        if (sold.isEmpty() || client == null) {
            _state.value = BillingState.PlansUnavailable
            return
        }
        val productList =
            sold.map {
                QueryProductDetailsParams.Product
                    .newBuilder()
                    .setProductId(it.id)
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
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _state.value = BillingState.PlansUnavailable
            return
        }
        val soldIds = sold.map { it.id }.toSet()
        val plans = details.filter { it.productId in soldIds }.flatMap { it.toPlans() }.distinct()
        prefs.storePlans(plans)
        products.clear()
        products.addAll(details)
        _state.value = plans.offeredPlans()?.let { BillingState.PlansLoaded(it) } ?: BillingState.PlansUnavailable
    }

    override fun purchase(
        productId: String,
        activity: Activity,
    ) {
        if (purchaseInProgress) return
        val client = billingClient
        val product = products.firstOrNull { it.productId == productId }
        if (client == null || product == null) {
            _state.value = BillingState.PurchaseFailed("Billing not ready for $productId")
            return
        }
        purchaseInProgress = true
        selectedProduct = product
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
    }

    override fun requestPurchaseHistory() {
        val client = billingClient
        if (client == null) {
            _purchaseHistory.value = PurchaseHistoryState.Failed
            return
        }
        client.queryPurchasesAsync(subscriptionsQuery()) { result, purchases ->
            val latest =
                purchases
                    .takeIf { result.responseCode == BillingClient.BillingResponseCode.OK }
                    ?.sortedByDescending { it.purchaseTime }
                    ?.firstOrNull { purchase -> products.any { it.productId == purchase.products.first() } }
            _purchaseHistory.value =
                latest?.let { PurchaseHistoryState.Success(it.purchaseToken, it.products.first()) } ?: PurchaseHistoryState.Failed
        }
    }

    override fun hasActiveSubscription(): Flow<Boolean> =
        callbackFlow {
            val client = billingClient
            if (client == null) {
                trySend(false)
                close()
                return@callbackFlow
            }
            client.queryPurchasesAsync(subscriptionsQuery()) { result, purchases ->
                trySend(
                    result.responseCode == BillingClient.BillingResponseCode.OK &&
                        purchases.any { it.isAutoRenewing && it.purchaseState == Purchase.PurchaseState.PURCHASED },
                )
            }
            awaitClose()
        }

    override fun showPaymentIssueMessages(activity: Activity) {
        val client = billingClient
        if (client?.isReady ==
            true
        ) {
            showInAppMessages(client, activity)
        } else {
            connect(activity) { billingClient?.let { showInAppMessages(it, activity) } }
        }
    }

    override suspend fun pendingPurchase(): PurchaseData? = prefs.purchase()

    override suspend fun clearPendingPurchase() = prefs.removePurchase()

    override suspend fun clearStoredData() = prefs.clear()

    override fun reset() {
        purchaseInProgress = false
        _state.value = if (isConnected()) BillingState.Ready else BillingState.NotConnected
    }

    private fun onPurchased(purchase: Purchase?) {
        val productId = purchase?.products?.firstOrNull { it == selectedProduct?.productId }
        val orderId = purchase?.orderId
        if (purchase == null || productId == null || orderId == null) {
            _state.value = BillingState.PurchaseFailed("Purchase did not match the selected product")
            return
        }
        appScope.launch {
            // Stored first, so the account can still be created if the app closes before signup ends.
            prefs.storePurchase(PurchaseData(purchase.purchaseToken, productId, orderId))
            _state.value = BillingState.Purchased
        }
    }

    private fun showInAppMessages(
        client: BillingClient,
        activity: Activity,
    ) {
        val params =
            InAppMessageParams
                .newBuilder()
                .addInAppMessageCategoryToShow(InAppMessageParams.InAppMessageCategoryId.TRANSACTIONAL)
                .build()
        client.showInAppMessages(activity, params) { result ->
            val token = result.purchaseToken
            if (result.responseCode == InAppMessageResult.InAppMessageResponseCode.SUBSCRIPTION_STATUS_UPDATED && !token.isNullOrBlank()) {
                _subscriptionStatusUpdates.tryEmit(token)
            }
        }
    }

    private fun subscriptionsQuery() = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()

    private fun ProductDetails.toPlans(): List<SubscriptionPlan> =
        subscriptionOfferDetails.orEmpty().mapNotNull { offer ->
            val phases = offer.pricingPhases.pricingPhaseList
            val paid = phases.firstOrNull { it.priceAmountMicros != 0L } ?: return@mapNotNull null
            SubscriptionPlan(
                id = productId,
                priceInMicros = paid.priceAmountMicros,
                formattedPrice = paid.formattedPrice,
                billingPeriod = paid.billingPeriod,
                currencyCode = paid.priceCurrencyCode,
                freeTrialDuration = phases.firstOrNull { it.priceAmountMicros == 0L }?.billingPeriod,
                plan =
                    when (paid.billingPeriod) {
                        BillingPeriod.MONTHLY -> MONTHLY_PLAN
                        BillingPeriod.YEARLY -> YEARLY_PLAN
                        else -> ""
                    },
            )
        }
}