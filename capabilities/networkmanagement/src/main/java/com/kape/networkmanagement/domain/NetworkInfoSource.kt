package com.kape.networkmanagement.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.net.ConnectivityManager.NetworkCallback
import android.net.NetworkCapabilities

interface NetworkInfoSource {

    fun registerDefaultCallback(callback: NetworkCallback, context: Context, receiver: BroadcastReceiver)

    fun registerCallback(callback: NetworkCallback)

    fun unregisterCallback(callback: NetworkCallback)

    fun triggerUpdate(callback: NetworkCallback)

    fun getSSID(networkCapabilities: NetworkCapabilities): String
}