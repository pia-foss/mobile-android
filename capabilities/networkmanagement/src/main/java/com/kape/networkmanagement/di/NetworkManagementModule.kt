package com.kape.networkmanagement.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.kape.networkmanagement.NetworkManagementPrefs
import com.kape.networkmanagement.data.NetworkInfoSourceImpl
import com.kape.networkmanagement.data.NetworkRulesManager
import com.kape.networkmanagement.data.NetworkScanner
import com.kape.networkmanagement.domain.NetworkInfoSource
import com.kape.networkmanagement.utils.NetworkUtil
import org.koin.dsl.module

val networkManagementModule = module {
    single { NetworkUtil(get()) }
    single { NetworkManagementPrefs(get()) }
    single { NetworkRulesManager(get(), get()) }
    single { NetworkScanner(get()) }
    single { provideConnectivityManager(get()) }
    single { provideWifiManager(get()) }
    single<NetworkInfoSource> { NetworkInfoSourceImpl(get(), get()) }
}

private fun provideConnectivityManager(context: Context): ConnectivityManager =
    context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager

private fun provideWifiManager(context: Context): WifiManager =
    context.getSystemService(Context.WIFI_SERVICE) as WifiManager