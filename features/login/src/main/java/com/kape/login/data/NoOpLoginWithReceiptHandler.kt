package com.kape.login.data

import com.kape.login.domain.mobile.LoginWithReceiptHandler
import com.kape.payments.utils.PurchaseHistoryState
import kotlinx.coroutines.flow.MutableStateFlow

class NoOpLoginWithReceiptHandler : LoginWithReceiptHandler {
    override val purchaseHistoryState: MutableStateFlow<PurchaseHistoryState> =
        MutableStateFlow(
            PurchaseHistoryState.Default,
        )

    override fun getPurchaseHistory() {
        // no-op
    }
}