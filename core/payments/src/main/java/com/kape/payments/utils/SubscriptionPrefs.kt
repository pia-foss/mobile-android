package com.kape.payments.utils

import android.content.Context
import com.kape.payments.data.models.PurchaseData
import com.kape.payments.data.models.Subscription
import com.kape.utils.Prefs
import kotlinx.serialization.json.Json

private const val AVAILABLE_SUBSCRIPTIONS = "available-subscriptions"
private const val PURCHASE_DATA = "purchase-data"

class SubscriptionPrefs(context: Context) : Prefs(context, "subscriptions") {

    fun storeSubscriptions(available: List<Subscription>) {
        val validData = mutableSetOf<String>()
        for (item in available) {
            if (!item.legacy) {
                validData.add(item.toString())
            }
        }
        prefs.edit().putStringSet(AVAILABLE_SUBSCRIPTIONS, validData).apply()
    }

    fun getSubscriptions(): List<Subscription> {
        val data = mutableListOf<Subscription>()
        for (item in prefs.getStringSet(AVAILABLE_SUBSCRIPTIONS, emptySet())!!) {
            data.add(Json.decodeFromString(item))
        }
        return data
    }

    fun storePurchaseData(purchaseData: PurchaseData) {
        prefs.edit().putString(PURCHASE_DATA, purchaseData.toString()).apply()
    }

    fun getPurchaseData(): PurchaseData? {
        val data = prefs.getString(PURCHASE_DATA, null)
        return if (data == null) {
            data
        } else {
            Json.decodeFromString(data)
        }
    }
}