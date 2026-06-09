package com.kape.dedicatedip.di

import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.dedicatedip.data.DipDataSourceImpl
import com.kape.dedicatedip.domain.ActivateDipUseCase
import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dedicatedip.domain.DipPurchaseHandler
import com.kape.dedicatedip.ui.vm.DipViewModel
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.DipPrefs
import com.kape.vpnregions.utils.RegionListProvider
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
class DipModule {
    @Singleton(binds = [DipDataSource::class])
    fun provideDipDataSource(
        accountApi: AndroidAccountAPI,
        dipPrefs: DipPrefs,
        @Named(DI.IO_SCOPE) ioScope: CoroutineScope,
    ): DipDataSource = DipDataSourceImpl(accountApi, dipPrefs, ioScope)

    @Singleton
    fun provideActivateDipUseCase(dataSource: DipDataSource): ActivateDipUseCase = ActivateDipUseCase(dataSource)

    @KoinViewModel
    fun provideDipViewModel(
        router: Router,
        regionListProvider: RegionListProvider,
        activateDipUseCase: ActivateDipUseCase,
        dipPrefs: DipPrefs,
        connectionPrefs: ConnectionPrefs,
        buildConfigProvider: BuildConfigProvider,
        connectionManager: ConnectionManager,
        dipPurchaseHandler: DipPurchaseHandler,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
    ): DipViewModel =
        DipViewModel(
            router,
            regionListProvider,
            activateDipUseCase,
            dipPrefs,
            connectionPrefs,
            buildConfigProvider,
            connectionManager,
            dipPurchaseHandler,
            ioDispatcher,
        )
}