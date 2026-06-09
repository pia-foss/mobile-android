package com.kape.login.domain.mobile

import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.payments.utils.PurchaseHistoryState
import kotlinx.coroutines.flow.MutableStateFlow

class LoginWithReceiptHandlerImpl(
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
) : LoginWithReceiptHandler {
    override val purchaseHistoryState: MutableStateFlow<PurchaseHistoryState> = vpnSubscriptionPaymentProvider.purchaseHistoryState

    override fun getPurchaseHistory() {
        vpnSubscriptionPaymentProvider.getPurchaseHistory()
    }
}