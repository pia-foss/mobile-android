package com.kape.connection.domain

import android.app.Activity
import com.kape.data.DI
import com.kape.payments.domain.RefreshSubscriptionStatusUseCase
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.payments.utils.InAppMessageState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named

class GooglePaymentIssueHandlerImpl(
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
    private val refreshSubscriptionStatusUseCase: RefreshSubscriptionStatusUseCase,
    @Named(DI.IO_SCOPE) private val ioScope: CoroutineScope,
) : PaymentIssueHandler {
    init {
        ioScope.launch {
            vpnSubscriptionPaymentProvider.inAppMessageState.collect { state ->
                if (state is InAppMessageState.SubscriptionStatusUpdated) {
                    refreshSubscriptionStatusUseCase.refresh(state.purchaseToken)
                }
            }
        }
    }

    override fun checkForPaymentIssues(activity: Activity) {
        vpnSubscriptionPaymentProvider.showInAppMessages(activity)
    }
}