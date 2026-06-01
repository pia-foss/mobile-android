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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    val dedicatedIps: StateFlow<List<DedicatedIPInformationResponse.DedicatedIPInformation>> =
        getDedicatedIps().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), emptyList())
    val purchasedSignupDipToken: StateFlow<String> =
        getPurchasedSignupDipToken().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), "")
    val dedicatedIpHomeBannerVisible: StateFlow<Boolean> =
        getDedicatedIpHomeBannerVisible().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), false)
    val dedicatedIpSignupPlans: StateFlow<DedicatedIpSignupPlans?> =
        getDedicatedIpSignupPlans().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), null)
    val dedicatedIpSupportedCountries: StateFlow<DedicatedIpSupportedCountries?> =
        getDedicatedIpSupportedCountries().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), null)
    val selectedDipSignupProductId: StateFlow<String> =
        getSelectedDipSignupProductId().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), "")
    val dedicatedIpSelectedCountry: StateFlow<DedicatedIpSelectedCountry?> =
        getDedicatedIpSelectedCountry().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), null)

    suspend fun addDedicatedIp(dip: DedicatedIPInformationResponse.DedicatedIPInformation) {
        dataStore.edit { prefs ->
            val current = prefs[DEDICATED_IPS] ?: emptySet()
            prefs[DEDICATED_IPS] = current + Json.encodeToString(dip)
        }
    }

    suspend fun removeDedicatedIp(dip: DedicatedIPInformationResponse.DedicatedIPInformation) {
        dataStore.edit { prefs ->
            val current = prefs[DEDICATED_IPS]?.toMutableSet() ?: mutableSetOf()
            current.remove(Json.encodeToString(dip))
            prefs[DEDICATED_IPS] = current
        }
    }

    suspend fun setPurchasedSignupDipToken(dipToken: String) {
        dataStore.edit { it[DIP_SIGNUP_PURCHASED_TOKEN] = dipToken }
    }

    suspend fun removePurchasedSignupDipToken() {
        dataStore.edit { it.remove(DIP_SIGNUP_PURCHASED_TOKEN) }
    }

    fun isDipSignupEnabled(isGoogleFlavor: Boolean): Flow<Boolean> =
        if (isGoogleFlavor) {
            dataStore.data.map { it[DIP_SIGNUP_ENABLED] ?: false }
        } else {
            flow { emit(false) }
        }

    suspend fun hideDedicatedIpHomeBanner() {
        dataStore.edit { it[DIP_SIGNUP_HOME_BANNER_VISIBLE] = false }
    }

    suspend fun setDedicatedIpSignupPlans(dedicatedIpSignupPlans: DedicatedIpSignupPlans) {
        dataStore.edit { it[DIP_SIGNUP_PLANS] = Json.encodeToString(dedicatedIpSignupPlans) }
    }

    suspend fun setDedicatedIpSupportedCountries(dedicatedIpSupportedCountries: DedicatedIpSupportedCountries) {
        dataStore.edit { it[DIP_SUPPORTED_COUNTRIES] = Json.encodeToString(dedicatedIpSupportedCountries) }
    }

    suspend fun setSelectedDipSignupProductId(productId: String) {
        dataStore.edit { it[DIP_SIGNUP_SELECTED_PRODUCT_ID] = productId }
    }

    suspend fun setDedicatedIpSelectedCountry(dedicatedIpSelectedCountry: DedicatedIpSelectedCountry) {
        dataStore.edit { it[DIP_SIGNUP_SELECTED_COUNTRY] = Json.encodeToString(dedicatedIpSelectedCountry) }
    }

    private fun getDedicatedIps(): Flow<List<DedicatedIPInformationResponse.DedicatedIPInformation>> =
        dataStore.data.map { prefs ->
            prefs[DEDICATED_IPS]?.map { Json.decodeFromString(it) } ?: emptyList()
        }

    private fun getPurchasedSignupDipToken(): Flow<String> = dataStore.data.map { it[DIP_SIGNUP_PURCHASED_TOKEN] ?: "" }

    private fun getDedicatedIpHomeBannerVisible(): Flow<Boolean> = dataStore.data.map { it[DIP_SIGNUP_HOME_BANNER_VISIBLE] ?: false }

    private fun getDedicatedIpSignupPlans(): Flow<DedicatedIpSignupPlans?> =
        dataStore.data.map { prefs ->
            prefs[DIP_SIGNUP_PLANS]?.let { Json.decodeFromString(it) }
        }

    private fun getDedicatedIpSupportedCountries(): Flow<DedicatedIpSupportedCountries?> =
        dataStore.data.map { prefs ->
            prefs[DIP_SUPPORTED_COUNTRIES]?.let { Json.decodeFromString(it) }
        }

    private fun getSelectedDipSignupProductId(): Flow<String> = dataStore.data.map { it[DIP_SIGNUP_SELECTED_PRODUCT_ID] ?: "" }

    private fun getDedicatedIpSelectedCountry(): Flow<DedicatedIpSelectedCountry?> =
        dataStore.data.map { prefs ->
            prefs[DIP_SIGNUP_SELECTED_COUNTRY]?.let { Json.decodeFromString(it) }
        }
}