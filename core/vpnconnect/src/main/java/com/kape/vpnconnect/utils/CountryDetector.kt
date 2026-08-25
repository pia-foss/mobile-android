package com.kape.vpnconnect.utils

import android.content.Context
import android.telephony.TelephonyManager
import org.koin.core.annotation.Singleton

val COUNTRY_LIST = listOf("CN", "IR", "RU")

@Singleton
class CountryDetector(
    private val context: Context,
) {
    fun detectCountry(): String? {
        // this works for devices with sim, for other devices we need an api
        return getNetworkCountryFromTelephony()
    }

    private fun getNetworkCountryFromTelephony(): String? {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val iso = tm?.networkCountryIso
        return if (!iso.isNullOrEmpty()) iso.uppercase() else null
    }
}