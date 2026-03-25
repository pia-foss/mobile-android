package com.kape.networkmanagement.di

import com.kape.localprefs.prefs.NetworkManagementPrefs
import com.kape.networkmanagement.data.NetworkRulesManager
import com.kape.networkmanagement.utils.NetworkUtil
import org.koin.dsl.module

val networkManagementModule = module {
    single { NetworkUtil(get()) }
    single { NetworkManagementPrefs(get()) }
    single { NetworkRulesManager(get(), get()) }
}