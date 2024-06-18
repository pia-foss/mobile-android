package com.kape.dip

import android.content.Context
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.dip.data.DedicatedIpSignupPlans
import com.kape.dip.data.DedicatedIpSupportedCountries
import com.kape.utils.Prefs
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val DEDICATED_IPS = "dedicated-ips"
private const val DIP_SIGNUP_ENABLED = "dip-signup-enabled"
private const val DIP_SIGNUP_HOME_BANNER_VISIBLE = "dip-signup-home-banner-visible"
private const val DIP_SIGNUP_PLANS = "dip-signup-plans"
private const val DIP_SUPPORTED_COUNTRIES = "dip-supported-countries"
private const val DIP_SIGNUP_PURCHASED_TOKEN = "dip-signup-purchased-token"
private const val DIP_SIGNUP_SELECTED_PRODUCT_ID = "dip-signup-selected-product-id"

class DipPrefs(
    context: Context,
    private val buildConfigProvider: BuildConfigProvider,
) : Prefs(context, "dip") {

    fun getDedicatedIps(): List<DedicatedIPInformationResponse.DedicatedIPInformation> {
        val dips = mutableListOf<DedicatedIPInformationResponse.DedicatedIPInformation>()
        for (dip in getDips()) {
            dips.add(Json.decodeFromString(dip))
        }
        return dips
    }

    fun addDedicatedIp(dip: DedicatedIPInformationResponse.DedicatedIPInformation) {
        val newDips = mutableSetOf<String>()
        newDips.addAll(getDips())
        newDips.add(Json.encodeToString(dip))
        saveDedicatedIps(newDips)
    }

    fun removeDedicatedIp(dip: DedicatedIPInformationResponse.DedicatedIPInformation) {
        val newDips = getDips().toMutableList()
        newDips.remove(Json.encodeToString(dip))
        saveDedicatedIps(newDips.toSet())
    }

    fun setPurchasedSignupDipToken(dipToken: String) {
        prefs.edit().putString(DIP_SIGNUP_PURCHASED_TOKEN, dipToken).apply()
    }

    fun getPurchasedSignupDipToken(): String =
        prefs.getString(DIP_SIGNUP_PURCHASED_TOKEN, "") ?: ""

    fun removePurchasedSignupDipToken() {
        prefs.edit().remove(DIP_SIGNUP_PURCHASED_TOKEN).apply()
    }

    fun isDipSignupEnabled() = if (buildConfigProvider.isGoogleFlavor()) {
        prefs.getBoolean(DIP_SIGNUP_ENABLED, false)
    } else {
        false
    }

    fun showDedicatedIpHomeBanner() = prefs.getBoolean(DIP_SIGNUP_HOME_BANNER_VISIBLE, false)

    fun hideDedicatedIpHomeBanner() {
        prefs.edit().putBoolean(DIP_SIGNUP_HOME_BANNER_VISIBLE, false).apply()
    }

    fun setDedicatedIpSignupPlans(dedicatedIpSignupPlans: DedicatedIpSignupPlans) {
        prefs.edit().putString(DIP_SIGNUP_PLANS, Json.encodeToString(dedicatedIpSignupPlans)).apply()
    }

    fun getDedicatedIpSignupPlans(): DedicatedIpSignupPlans? =
        prefs.getString(DIP_SIGNUP_PLANS, null)?.let {
            Json.decodeFromString(it)
        }

    fun setDedicatedIpSupportedCountries(
        dedicatedIpSupportedCountries: DedicatedIpSupportedCountries,
    ) {
        prefs.edit().putString(
            DIP_SUPPORTED_COUNTRIES,
            Json.encodeToString(dedicatedIpSupportedCountries),
        ).apply()
    }

    fun getDedicatedIpSupportedCountries(): DedicatedIpSupportedCountries? =
        prefs.getString(DIP_SUPPORTED_COUNTRIES, null)?.let {
            Json.decodeFromString(it)
        }

    fun setSelectedDipSignupProductId(productId: String) {
        prefs.edit().putString(
            DIP_SIGNUP_SELECTED_PRODUCT_ID,
            productId,
        ).apply()
    }

    fun getSelectedDipSignupProductId(): String =
        prefs.getString(DIP_SIGNUP_SELECTED_PRODUCT_ID, "") ?: ""

    private fun getDips() = prefs.getStringSet(DEDICATED_IPS, emptySet()) ?: emptySet()

    private fun saveDedicatedIps(dips: Set<String>) =
        prefs.edit().putStringSet(DEDICATED_IPS, dips).apply()
}