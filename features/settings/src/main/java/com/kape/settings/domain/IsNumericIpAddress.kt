package com.kape.settings.domain

import android.net.InetAddresses
import android.os.Build
import android.util.Patterns
import org.koin.core.annotation.Factory

fun interface IsNumericIpAddress {
    operator fun invoke(address: String): Boolean
}

@Factory(binds = [IsNumericIpAddress::class])
internal class AndroidIsNumericIpAddress : IsNumericIpAddress {
    override fun invoke(address: String): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            InetAddresses.isNumericAddress(address)
        } else {
            Patterns.IP_ADDRESS.matcher(address).matches()
        }
}