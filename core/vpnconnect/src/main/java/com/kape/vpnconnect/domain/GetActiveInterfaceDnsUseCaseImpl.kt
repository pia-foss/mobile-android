package com.kape.vpnconnect.domain

import android.content.Context
import android.net.ConnectivityManager
import org.koin.core.annotation.Singleton

@Singleton(binds = [GetActiveInterfaceDnsUseCase::class])
class GetActiveInterfaceDnsUseCaseImpl(private val context: Context) : GetActiveInterfaceDnsUseCase {

    override fun invoke(): List<String> {
        val result = mutableListOf<String>()
        val connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        connectivityManager.getLinkProperties(network)?.dnsServers?.let { dnsServers ->
            dnsServers.forEach { inetAddress ->
                inetAddress.hostAddress?.let {
                    result.add(it)
                }
            }
        }
        return result
    }
}