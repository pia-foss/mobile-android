package com.kape.vpnconnect.utils

import android.content.Context
import android.telephony.TelephonyManager
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

val COUNTRY_LIST = listOf("CN", "IR", "RU")

@Singleton
class CountryDetector(
    private val context: Context,
    private val accountAPI: AndroidAccountAPI,
) {
    suspend fun detectCountry(): String? = getNetworkCountryFromTelephony() ?: getCountry()

    private suspend fun getCountry(): String? =
        suspendCancellableCoroutine { cont ->
            accountAPI.locationInfo { locationInfo, error ->
                if (locationInfo != null) {
                    cont.resume(locationInfo.country.uppercase())
                } else {
                    cont.resume(null)
                }
            }
        }

    private fun getNetworkCountryFromTelephony(): String? {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val iso = tm?.networkCountryIso
        return if (!iso.isNullOrEmpty()) iso.uppercase() else null
    }
}