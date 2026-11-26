package com.kape.login.data.store

import android.app.Activity
import android.content.Context
import com.kape.account.domain.usecase.LoginWithReceiptUseCase
import com.kape.contracts.PaymentIssueChecker
import com.kape.contracts.PurchaseEvents
import com.kape.contracts.PurchaseHistoryState
import com.kape.contracts.StorePurchaseHistory
import com.kape.contracts.di.Qualifiers
import com.kape.login.domain.StoreDataCleaner
import com.kape.payments.domain.VpnSubscriptionBilling
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Singleton(binds = [StorePurchaseHistory::class])
internal class PlayStorePurchaseHistory(
    private val billing: VpnSubscriptionBilling,
    private val events: PurchaseEvents,
) : StorePurchaseHistory {
    override val state: StateFlow<PurchaseHistoryState> = billing.purchaseHistory

    override fun request() = billing.requestPurchaseHistory()

    override suspend fun onReceiptLoginSucceeded() = events.purchaseSucceeded()
}

@Singleton(binds = [PaymentIssueChecker::class])
internal class PlayPaymentIssueChecker(
    private val billing: VpnSubscriptionBilling,
    private val loginWithReceipt: LoginWithReceiptUseCase,
    private val context: Context,
    @Named(Qualifiers.APP_SCOPE) appScope: CoroutineScope,
) : PaymentIssueChecker {
    init {
        appScope.launch {
            billing.subscriptionStatusUpdates.collect { purchaseToken ->
                billing.pendingPurchase()?.productId?.let { productId ->
                    loginWithReceipt(purchaseToken, productId, context.packageName)
                }
            }
        }
    }

    override fun checkForPaymentIssues(activity: Activity) = billing.showPaymentIssueMessages(activity)
}

@Singleton(binds = [StoreDataCleaner::class])
internal class PlayStoreDataCleaner(
    private val billing: VpnSubscriptionBilling,
) : StoreDataCleaner {
    override suspend fun clear() = billing.clearStoredData()
}