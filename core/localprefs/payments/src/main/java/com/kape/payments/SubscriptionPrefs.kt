package com.kape.payments

import android.content.Context
import com.kape.payments.data.DipPurchaseData
import com.kape.payments.data.PurchaseData
import com.kape.payments.data.Subscription
import com.kape.utils.Prefs
import kotlinx.serialization.json.Json

private const val AVAILABLE_VPN_SUBSCRIPTIONS = "available-subscriptions"
private const val VPN_PURCHASE_DATA = "purchase-data"
private const val DIP_PURCHASE_DATA = "dip-purchase-data"

class SubscriptionPrefs(context: Context) : Prefs(context, "subscriptions") {

    fun storeVpnSubscriptions(available: List<Subscription>) {
        val validData = mutableSetOf<String>()
        for (item in available) {
            if (!item.legacy) {
                validData.add(item.toString())
            }
        }
        prefs.edit().putStringSet(AVAILABLE_VPN_SUBSCRIPTIONS, validData).apply()
    }

    fun getVpnSubscriptions(): List<Subscription> {
        val data = mutableListOf<Subscription>()
        for (item in prefs.getStringSet(AVAILABLE_VPN_SUBSCRIPTIONS, emptySet())!!) {
            data.add(Json.decodeFromString(item))
        }
        return data
    }

    fun storeVpnPurchaseData(purchaseData: PurchaseData) {
        prefs.edit().putString(VPN_PURCHASE_DATA, purchaseData.toString()).apply()
    }

    fun getVpnPurchaseData(): PurchaseData? {
        val data = prefs.getString(VPN_PURCHASE_DATA, null)
        return if (data == null) {
            data
        } else {
            Json.decodeFromString(data)
        }
    }

    fun storeDipPurchaseData(purchaseData: DipPurchaseData) {
        prefs.edit().putString(DIP_PURCHASE_DATA, purchaseData.toString()).apply()
    }

    fun getDipPurchaseData(): DipPurchaseData? =
        prefs.getString(DIP_PURCHASE_DATA, null)?.let {
            Json.decodeFromString(it)
        }

    fun removeDipPurchaseData() {
        prefs.edit().remove(DIP_PURCHASE_DATA).apply()
    }
}