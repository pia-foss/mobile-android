package com.kape.settings.domain

import android.net.InetAddresses
import android.os.Build
import android.util.Patterns

class IsNumericIpAddressUseCaseImpl : IsNumericIpAddressUseCase {

    override fun invoke(ipAddress: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            InetAddresses.isNumericAddress(ipAddress)
        } else {
            Patterns.IP_ADDRESS.matcher(ipAddress).matches()
        }
    }
}