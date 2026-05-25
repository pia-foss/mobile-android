package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.kape.dip.data.DedicatedIpSelectedCountry
import com.kape.dip.data.DedicatedIpSignupPlans
import com.kape.dip.data.DedicatedIpSupportedCountries
import com.kape.localprefs.Prefs
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

private val DEDICATED_IPS = stringSetPreferencesKey("dedicated-ips")
private val DIP_SIGNUP_ENABLED = booleanPreferencesKey("dip-signup-enabled")
private val DIP_SIGNUP_HOME_BANNER_VISIBLE = booleanPreferencesKey("dip-signup-home-banner-visible")
private val DIP_SIGNUP_PLANS = stringPreferencesKey("dip-signup-plans")
private val DIP_SUPPORTED_COUNTRIES = stringPreferencesKey("dip-supported-countries")
private val DIP_SIGNUP_PURCHASED_TOKEN = stringPreferencesKey("dip-signup-purchased-token")
private val DIP_SIGNUP_SELECTED_PRODUCT_ID = stringPreferencesKey("dip-signup-selected-product-id")
private val DIP_SIGNUP_SELECTED_COUNTRY = stringPreferencesKey("dip-signup-selected-country")

@Singleton
class DipPrefs(
    context: Context,
) : Prefs(context, "dip") {
    fun getDedicatedIps(): Flow<List<DedicatedIPInformationResponse.DedicatedIPInformation>> =
        dataStore.data.map { prefs ->
            prefs[DEDICATED_IPS]?.map { Json.decodeFromString(it) } ?: emptyList()
        }

    fun addDedicatedIp(dip: DedicatedIPInformationResponse.DedicatedIPInformation) {
        scope.launch {
            dataStore.edit { prefs ->
                val current = prefs[DEDICATED_IPS] ?: emptySet()
                prefs[DEDICATED_IPS] = current + Json.encodeToString(dip)
            }
        }
    }

    fun removeDedicatedIp(dip: DedicatedIPInformationResponse.DedicatedIPInformation) {
        scope.launch {
            dataStore.edit { prefs ->
                val current = prefs[DEDICATED_IPS]?.toMutableSet() ?: mutableSetOf()
                current.remove(Json.encodeToString(dip))
                prefs[DEDICATED_IPS] = current
            }
        }
    }

    fun setPurchasedSignupDipToken(dipToken: String) {
        scope.launch {
            dataStore.edit { it[DIP_SIGNUP_PURCHASED_TOKEN] = dipToken }
        }
    }

    fun getPurchasedSignupDipToken(): Flow<String> = dataStore.data.map { it[DIP_SIGNUP_PURCHASED_TOKEN] ?: "" }

    fun removePurchasedSignupDipToken() {
        scope.launch {
            dataStore.edit { it.remove(DIP_SIGNUP_PURCHASED_TOKEN) }
        }
    }

    fun isDipSignupEnabled(isGoogleFlavor: Boolean): Flow<Boolean> =
        if (isGoogleFlavor) {
            dataStore.data.map { it[DIP_SIGNUP_ENABLED] ?: false }
        } else {
            flow { emit(false) }
        }

    fun showDedicatedIpHomeBanner(): Flow<Boolean> = dataStore.data.map { it[DIP_SIGNUP_HOME_BANNER_VISIBLE] ?: false }

    fun hideDedicatedIpHomeBanner() {
        scope.launch {
            dataStore.edit { it[DIP_SIGNUP_HOME_BANNER_VISIBLE] = false }
        }
    }

    fun setDedicatedIpSignupPlans(dedicatedIpSignupPlans: DedicatedIpSignupPlans) {
        scope.launch {
            dataStore.edit { it[DIP_SIGNUP_PLANS] = Json.encodeToString(dedicatedIpSignupPlans) }
        }
    }

    fun getDedicatedIpSignupPlans(): Flow<DedicatedIpSignupPlans?> =
        dataStore.data.map { prefs ->
            prefs[DIP_SIGNUP_PLANS]?.let { Json.decodeFromString(it) }
        }

    fun setDedicatedIpSupportedCountries(dedicatedIpSupportedCountries: DedicatedIpSupportedCountries) {
        scope.launch {
            dataStore.edit { it[DIP_SUPPORTED_COUNTRIES] = Json.encodeToString(dedicatedIpSupportedCountries) }
        }
    }

    fun getDedicatedIpSupportedCountries(): Flow<DedicatedIpSupportedCountries?> =
        dataStore.data.map { prefs ->
            prefs[DIP_SUPPORTED_COUNTRIES]?.let { Json.decodeFromString(it) }
        }

    fun setSelectedDipSignupProductId(productId: String) {
        scope.launch {
            dataStore.edit { it[DIP_SIGNUP_SELECTED_PRODUCT_ID] = productId }
        }
    }

    fun getSelectedDipSignupProductId(): Flow<String> = dataStore.data.map { it[DIP_SIGNUP_SELECTED_PRODUCT_ID] ?: "" }

    fun setDedicatedIpSelectedCountry(dedicatedIpSelectedCountry: DedicatedIpSelectedCountry) {
        scope.launch {
            dataStore.edit { it[DIP_SIGNUP_SELECTED_COUNTRY] = Json.encodeToString(dedicatedIpSelectedCountry) }
        }
    }

    fun getDedicatedIpSelectedCountry(): Flow<DedicatedIpSelectedCountry?> =
        dataStore.data.map { prefs ->
            prefs[DIP_SIGNUP_SELECTED_COUNTRY]?.let { Json.decodeFromString(it) }
        }
}