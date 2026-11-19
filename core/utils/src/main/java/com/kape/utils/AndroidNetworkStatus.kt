package com.kape.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.kape.contracts.NetworkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Singleton

@Singleton(binds = [NetworkStatus::class])
internal class AndroidNetworkStatus(
    context: Context,
) : NetworkStatus {
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    private val online = MutableStateFlow(connectivityManager.hasInternet(connectivityManager.activeNetwork))
    override val isOnline: StateFlow<Boolean> = online.asStateFlow()

    init {
        connectivityManager.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities,
                ) {
                    online.value = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                }

                override fun onLost(network: Network) {
                    online.value = false
                }
            },
        )
    }

    private fun ConnectivityManager.hasInternet(network: Network?): Boolean =
        network != null && getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}