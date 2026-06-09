package com.kape.payments.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.kape.data.model.DipPurchaseData
import com.kape.data.model.PurchaseData
import com.kape.data.model.Subscription
import com.kape.localprefs.Prefs
import com.kape.payments.data.SubscriptionPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

private val AVAILABLE_VPN_SUBSCRIPTIONS = stringSetPreferencesKey("available-subscriptions")
private val AVAILABLE_VPN_SUBSCRIPTIONS_V2 = stringSetPreferencesKey("available-subscriptions-v2")
private val VPN_PURCHASE_DATA = stringPreferencesKey("purchase-data")
private val DIP_PURCHASE_DATA = stringPreferencesKey("dip-purchase-data")

@Singleton
class SubscriptionPrefs(
    context: Context,
) : Prefs(context, "subscriptions") {
    val vpnSubscriptions: StateFlow<List<Subscription>> =
        getVpnSubscriptions().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), emptyList())
    val vpnSubscriptionPlans: StateFlow<List<SubscriptionPlan>> =
        getVpnSubscriptionPlans().stateIn(
            scope,
            SharingStarted.WhileSubscribed(waitTime),
            emptyList(),
        )
    val vpnPurchaseData: StateFlow<PurchaseData?> =
        getVpnPurchaseData().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), null)
    val dipPurchaseData: StateFlow<DipPurchaseData?> =
        getDipPurchaseData().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), null)

    suspend fun storeVpnSubscriptions(available: List<Subscription>) {
        val validData = available.filter { !it.legacy }.map { it.toString() }.toSet()
        dataStore.edit { it[AVAILABLE_VPN_SUBSCRIPTIONS] = validData }
    }

    suspend fun storeVpnSubscriptionPlans(available: List<SubscriptionPlan>) {
        val validData = available.map { it.toString() }.toSet()
        dataStore.edit { it[AVAILABLE_VPN_SUBSCRIPTIONS_V2] = validData }
    }

    suspend fun storeVpnPurchaseData(purchaseData: PurchaseData) {
        dataStore.edit { it[VPN_PURCHASE_DATA] = purchaseData.toString() }
    }

    suspend fun storeDipPurchaseData(purchaseData: DipPurchaseData) {
        dataStore.edit { it[DIP_PURCHASE_DATA] = Json.encodeToString(purchaseData) }
    }

    suspend fun removeDipPurchaseData() {
        dataStore.edit { it.remove(DIP_PURCHASE_DATA) }
    }

    private fun getVpnSubscriptions(): Flow<List<Subscription>> =
        dataStore.data.map { prefs ->
            prefs[AVAILABLE_VPN_SUBSCRIPTIONS]?.map { Json.decodeFromString(it) } ?: emptyList()
        }

    private fun getVpnSubscriptionPlans(): Flow<List<SubscriptionPlan>> =
        dataStore.data.map { prefs ->
            prefs[AVAILABLE_VPN_SUBSCRIPTIONS_V2]?.map { Json.decodeFromString(it) } ?: emptyList()
        }

    private fun getVpnPurchaseData(): Flow<PurchaseData?> =
        dataStore.data.map { prefs ->
            prefs[VPN_PURCHASE_DATA]?.let { Json.decodeFromString(it) }
        }

    private fun getDipPurchaseData(): Flow<DipPurchaseData?> =
        dataStore.data.map { prefs ->
            prefs[DIP_PURCHASE_DATA]?.let { Json.decodeFromString(it) }
        }
}