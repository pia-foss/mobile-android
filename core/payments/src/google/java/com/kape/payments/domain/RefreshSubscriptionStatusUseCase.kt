package com.kape.payments.domain

import android.content.Context
import com.kape.contracts.AuthenticationDataSource
import com.kape.payments.prefs.SubscriptionPrefs
import org.koin.core.annotation.Singleton

@Singleton
class RefreshSubscriptionStatusUseCase(
    private val authenticationDataSource: AuthenticationDataSource,
    private val prefs: SubscriptionPrefs,
    private val context: Context,
) {
    suspend fun refresh(purchaseToken: String) {
        prefs.vpnPurchaseData.value?.productId?.let {
            authenticationDataSource.loginWithReceipt(purchaseToken, it, context.packageName)
        }
    }
}