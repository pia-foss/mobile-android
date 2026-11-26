package com.kape.automation.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.kape.automation.domain.ApplyNetworkRuleUseCase
import com.kape.automation.domain.NetworkObserver
import com.kape.contracts.di.Qualifiers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

// Also bound as itself: AutomationService creates it to start watching networks.
@Singleton(binds = [NetworkMonitor::class, NetworkObserver::class])
class NetworkMonitor(
    context: Context,
    private val applyNetworkRule: ApplyNetworkRuleUseCase,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) : NetworkObserver {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _ssid = MutableStateFlow<String?>(null)

    override val ssid: StateFlow<String?> = _ssid.asStateFlow()

    // onCapabilitiesChanged also fires for things like signal strength, so rules are only applied
    // when the network itself changes.
    @Volatile
    private var lastHandledNetwork: HandledNetwork? = null

    private val networkCallback =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            @RequiresApi(Build.VERSION_CODES.S)
            object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities,
                ) = onNetwork(network, capabilities)
            }
        } else {
            object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities,
                ) = onNetwork(network, capabilities)
            }
        }

    // The underlying Wi-Fi or mobile network rather than the default one, which becomes our own VPN
    // once it is up and would hide the network's name and switches between networks.
    private val networkRequest =
        NetworkRequest
            .Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

    init {
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    override fun reapplyRules() {
        lastHandledNetwork = null
        connectivityManager.unregisterNetworkCallback(networkCallback)
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    private fun onNetwork(
        network: Network,
        capabilities: NetworkCapabilities,
    ) {
        val isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val ssid = if (isWifi) ssidOf(capabilities) else ""
        if (isWifi) _ssid.value = ssid

        val current = HandledNetwork(network, ssid, isWifi)
        val isNetworkChange = networkChange(lastHandledNetwork, current) ?: return
        lastHandledNetwork = current
        appScope.launch { applyNetworkRule(ssid, isWifi, isNetworkChange) }
    }

    @Suppress("DEPRECATION")
    private fun ssidOf(capabilities: NetworkCapabilities): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            (capabilities.transportInfo as? WifiInfo)?.ssid.orEmpty()
        } else {
            wifiManager.connectionInfo.ssid.orEmpty()
        }
}

internal data class HandledNetwork(
    val network: Network,
    val ssid: String,
    val isWifi: Boolean,
)

/**
 * Null when [current] is the network already handled, so its rule isn't applied again. Otherwise
 * whether it is a switch to a different network, rather than the first one seen or the same
 * network changing its name or type.
 */
internal fun networkChange(
    previous: HandledNetwork?,
    current: HandledNetwork,
): Boolean? = if (current == previous) null else previous != null && previous.network != current.network