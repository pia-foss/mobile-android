package com.kape.appbar.di

import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.contracts.ConnectionStatusProvider
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.utils.NetworkConnectionListener
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named

@Module
class AppBarModule {
    @KoinViewModel
    fun provideAppBarViewModel(
        router: Router,
        connectionStatusProvider: ConnectionStatusProvider,
        networkConnectionListener: NetworkConnectionListener,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
    ): AppBarViewModel = AppBarViewModel(router, connectionStatusProvider, ioDispatcher, networkConnectionListener)
}