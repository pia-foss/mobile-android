package com.kape.dip

import android.content.Context
import com.kape.dip.data.DedicatedIpSignupPlans
import com.kape.utils.Prefs
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val DEDICATED_IPS = "dedicated-ips"
private const val DIP_SIGNUP_ENABLED = "dip-signup-enabled"
private const val DIP_SIGNUP_HOME_BANNER_VISIBLE = "dip-signup-home-banner-visible"
private const val DIP_SIGNUP_PLANS = "dip-signup-plans"
private const val DIP_SIGNUP_PURCHASED_TOKEN = "dip-signup-purchased-token"

class DipPrefs(context: Context) : Prefs(context, "dip") {

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

    fun isDipSignupEnabled() = prefs.getBoolean(DIP_SIGNUP_ENABLED, false)

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

    private fun getDips() = prefs.getStringSet(DEDICATED_IPS, emptySet()) ?: emptySet()

    private fun saveDedicatedIps(dips: Set<String>) =
        prefs.edit().putStringSet(DEDICATED_IPS, dips).apply()
}