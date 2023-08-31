package com.kape.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

sealed class InternetConnectionState {
    data object Connected : InternetConnectionState()
    data object Disconnected : InternetConnectionState()
}

val Context.currentConnectivityState: InternetConnectionState
    get() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return getCurrentConnectivityState(connectivityManager)
    }

private fun getCurrentConnectivityState(
    connectivityManager: ConnectivityManager,
): InternetConnectionState {
    val connected = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    return if (connected) InternetConnectionState.Connected else InternetConnectionState.Disconnected
}

@ExperimentalCoroutinesApi
fun Context.observeConnectivityAsFlow() = callbackFlow {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val callback = NetworkCallback { connectionState -> trySend(connectionState) }

    val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    connectivityManager.registerNetworkCallback(networkRequest, callback)

    val currentState = getCurrentConnectivityState(connectivityManager)
    trySend(currentState)

    awaitClose {
        connectivityManager.unregisterNetworkCallback(callback)
    }
}

fun NetworkCallback(callback: (InternetConnectionState) -> Unit): ConnectivityManager.NetworkCallback {
    return object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            callback(InternetConnectionState.Connected)
        }

        override fun onLost(network: Network) {
            callback(InternetConnectionState.Disconnected)
        }
    }
}