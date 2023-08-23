package com.kape.payments.domain

import com.kape.payments.SubscriptionPrefs
import com.kape.payments.data.PurchaseData

class GetPurchaseDetailsUseCase(private val prefs: SubscriptionPrefs) {

    fun getPurchaseDetails(): PurchaseData? = prefs.getPurchaseData()
}