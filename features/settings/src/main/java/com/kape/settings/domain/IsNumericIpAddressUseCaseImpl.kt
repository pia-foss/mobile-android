package com.kape.settings.domain

import android.net.InetAddresses
import android.os.Build
import android.util.Patterns
import org.koin.core.annotation.Singleton

@Singleton([IsNumericIpAddressUseCase::class])
class IsNumericIpAddressUseCaseImpl : IsNumericIpAddressUseCase {
    override fun invoke(ipAddress: String): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            InetAddresses.isNumericAddress(ipAddress)
        } else {
            Patterns.IP_ADDRESS.matcher(ipAddress).matches()
        }
}