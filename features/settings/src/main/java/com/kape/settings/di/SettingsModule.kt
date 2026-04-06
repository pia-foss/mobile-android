package com.kape.settings.di

import com.kape.contracts.AppInfo
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.KpiDataSource
import com.kape.contracts.Router
import com.kape.csi.domain.SendLogUseCase
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.location.data.LocationPermissionManager
import com.kape.settings.domain.IsNumericIpAddressUseCase
import com.kape.settings.domain.IsNumericIpAddressUseCaseImpl
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.GetLogsUseCase
import com.kape.vpnconnect.domain.ReconnectUseCase
import com.kape.vpnregions.data.VpnRegionRepository
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class SettingsModule {

    @Singleton(binds = [IsNumericIpAddressUseCase::class])
    fun provideIsNumericIpAddressUseCase(): IsNumericIpAddressUseCase =
        IsNumericIpAddressUseCaseImpl()

    @KoinViewModel
    fun provideSettingsViewModel(
        router: Router,
        appInfo: AppInfo,
        prefs: SettingsPrefs,
        connectionPrefs: ConnectionPrefs,
        csiPrefs: CsiPrefs,
        regionsRepository: VpnRegionRepository,
        kpiDataSource: KpiDataSource,
        connectionDataSource: ConnectionDataSource,
        getDebugLogsUseCase: GetLogsUseCase,
        sendLogUseCase: SendLogUseCase,
        isNumericIpAddressUseCase: IsNumericIpAddressUseCase,
        locationPermissionManager: LocationPermissionManager,
        reconnectUseCase: ReconnectUseCase,
        connectionInfoProvider: ConnectionInfoProvider,
    ): SettingsViewModel = SettingsViewModel(
        router, appInfo, prefs, connectionPrefs, csiPrefs, regionsRepository, kpiDataSource,
        connectionDataSource, getDebugLogsUseCase, sendLogUseCase, isNumericIpAddressUseCase,
        locationPermissionManager, reconnectUseCase, connectionInfoProvider,
    )
}