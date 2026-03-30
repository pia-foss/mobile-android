package com.kape.appbar.di

import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.contracts.Router
import com.kape.utils.NetworkConnectionListener
import com.kape.vpnconnect.utils.ConnectionManager
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class AppBarModule {

    @KoinViewModel
    fun provideAppBarViewModel(
        router: Router,
        connectionManager: ConnectionManager,
        networkConnectionListener: NetworkConnectionListener,
    ): AppBarViewModel = AppBarViewModel(router, connectionManager, networkConnectionListener)
}