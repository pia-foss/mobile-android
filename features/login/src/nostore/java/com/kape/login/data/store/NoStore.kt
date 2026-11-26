package com.kape.login.data.store

import com.kape.contracts.PaymentIssueChecker
import com.kape.contracts.PurchaseHistoryState
import com.kape.contracts.StorePurchaseHistory
import com.kape.login.domain.StoreDataCleaner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Singleton

@Singleton(binds = [StorePurchaseHistory::class])
internal class NoStorePurchaseHistory : StorePurchaseHistory {
    override val state: StateFlow<PurchaseHistoryState> = MutableStateFlow(PurchaseHistoryState.Default)

    override fun request() = Unit

    override suspend fun onReceiptLoginSucceeded() = Unit
}

@Singleton(binds = [PaymentIssueChecker::class])
internal class NoPaymentIssueChecker : PaymentIssueChecker {
    override fun checkForPaymentIssues(activity: android.app.Activity) = Unit
}

@Singleton(binds = [StoreDataCleaner::class])
internal class NoStoreDataCleaner : StoreDataCleaner {
    override suspend fun clear() = Unit
}