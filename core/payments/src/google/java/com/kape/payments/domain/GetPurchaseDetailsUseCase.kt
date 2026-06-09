package com.kape.payments.domain

import com.kape.payments.prefs.SubscriptionPrefs
import org.koin.core.annotation.Singleton

@Singleton
class GetPurchaseDetailsUseCase(
    private val prefs: SubscriptionPrefs,
) {
    fun getPurchaseDetails() = prefs.vpnPurchaseData
}