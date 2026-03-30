package com.kape.networkmanagement.di

import android.content.Context
import com.kape.localprefs.prefs.NetworkManagementPrefs
import com.kape.networkmanagement.data.NetworkRulesManager
import com.kape.networkmanagement.utils.NetworkUtil
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class NetworkManagementModule {

    @Singleton
    fun provideNetworkUtil(context: Context): NetworkUtil =
        NetworkUtil(context)

    @Singleton
    fun provideNetworkRulesManager(
        prefs: NetworkManagementPrefs,
        util: NetworkUtil,
    ): NetworkRulesManager = NetworkRulesManager(prefs, util)
}