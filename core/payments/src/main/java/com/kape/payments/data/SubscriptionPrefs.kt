package com.kape.payments.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.kape.contracts.di.Qualifiers
import com.kape.data.model.DipPurchaseData
import com.kape.data.model.PurchaseData
import com.kape.data.model.Subscription
import com.kape.localprefs.Prefs
import com.kape.payments.domain.SubscriptionPlan
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

// Key names kept from mobile-android, so purchases stored by the old app are still found.
private val AVAILABLE_VPN_SUBSCRIPTIONS = stringSetPreferencesKey("available-subscriptions")
private val AVAILABLE_VPN_SUBSCRIPTIONS_V2 = stringSetPreferencesKey("available-subscriptions-v2")
private val VPN_PURCHASE_DATA = stringPreferencesKey("purchase-data")
private val DIP_PURCHASE_DATA = stringPreferencesKey("dip-purchase-data")

@Singleton
internal class SubscriptionPrefs(
    context: Context,
    @Named(Qualifiers.APP_SCOPE) appScope: CoroutineScope,
    @Named(Qualifiers.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
) : Prefs(context, "subscriptions", appScope, ioDispatcher) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun subscriptions(): List<Subscription> =
        dataStore.data.map { prefs -> prefs[AVAILABLE_VPN_SUBSCRIPTIONS].orEmpty().mapNotNull { it.decodeOrNull<Subscription>() } }.first()

    suspend fun storeSubscriptions(subscriptions: List<Subscription>) {
        dataStore.edit { prefs ->
            prefs[AVAILABLE_VPN_SUBSCRIPTIONS] =
                subscriptions.filterNot { it.legacy }.map { json.encodeToString(it) }.toSet()
        }
    }

    suspend fun plans(): List<SubscriptionPlan> =
        dataStore.data
            .map { prefs ->
                prefs[AVAILABLE_VPN_SUBSCRIPTIONS_V2].orEmpty().mapNotNull { it.decodeOrNull<SubscriptionPlan>() }
            }.first()

    suspend fun storePlans(plans: List<SubscriptionPlan>) {
        dataStore.edit { prefs -> prefs[AVAILABLE_VPN_SUBSCRIPTIONS_V2] = plans.map { json.encodeToString(it) }.toSet() }
    }

    suspend fun purchase(): PurchaseData? = dataStore.data.map { it[VPN_PURCHASE_DATA]?.decodeOrNull<PurchaseData>() }.first()

    suspend fun storePurchase(purchase: PurchaseData) {
        dataStore.edit { it[VPN_PURCHASE_DATA] = json.encodeToString(purchase) }
    }

    suspend fun removePurchase() {
        dataStore.edit { it.remove(VPN_PURCHASE_DATA) }
    }

    suspend fun dipPurchase(): DipPurchaseData? = dataStore.data.map { it[DIP_PURCHASE_DATA]?.decodeOrNull<DipPurchaseData>() }.first()

    suspend fun storeDipPurchase(purchase: DipPurchaseData) {
        dataStore.edit { it[DIP_PURCHASE_DATA] = json.encodeToString(purchase) }
    }

    suspend fun removeDipPurchase() {
        dataStore.edit { it.remove(DIP_PURCHASE_DATA) }
    }

    private inline fun <reified T> String.decodeOrNull(): T? = runCatching { json.decodeFromString<T>(this) }.getOrNull()
}