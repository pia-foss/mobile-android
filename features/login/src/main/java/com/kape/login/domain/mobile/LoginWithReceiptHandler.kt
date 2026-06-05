package com.kape.login.domain.mobile

import com.kape.payments.utils.PurchaseHistoryState
import kotlinx.coroutines.flow.MutableStateFlow

interface LoginWithReceiptHandler {
    val purchaseHistoryState: MutableStateFlow<PurchaseHistoryState>

    fun getPurchaseHistory()
}