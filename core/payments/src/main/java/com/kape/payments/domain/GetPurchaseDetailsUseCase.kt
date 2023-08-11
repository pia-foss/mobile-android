package com.kape.payments.domain

import com.kape.payments.data.models.PurchaseData
import com.kape.payments.utils.SubscriptionPrefs

class GetPurchaseDetailsUseCase(private val prefs: SubscriptionPrefs) {

    fun getPurchaseDetails(): PurchaseData? = prefs.getPurchaseData()
}