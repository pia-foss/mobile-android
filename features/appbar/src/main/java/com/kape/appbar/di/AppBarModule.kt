package com.kape.appbar.di

import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.contracts.Router
import com.kape.utils.NetworkConnectionListener
import com.kape.vpnconnect.utils.ConnectionStatusProvider
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class AppBarModule {

    @KoinViewModel
    fun provideAppBarViewModel(
        router: Router,
        connectionStatusProvider: ConnectionStatusProvider,
        networkConnectionListener: NetworkConnectionListener,
    ): AppBarViewModel =
        AppBarViewModel(router, connectionStatusProvider, networkConnectionListener)
}