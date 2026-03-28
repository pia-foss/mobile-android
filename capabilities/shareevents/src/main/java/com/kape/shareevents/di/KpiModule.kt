package com.kape.shareevents.di

import com.kape.contracts.ConfigInfo
import com.kape.contracts.KpiDataSource
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.shareevents.data.KpiDataSourceImpl
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.privateinternetaccess.kpi.KPIAPI
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class KpiModule {

    @Singleton(binds = [KpiDataSource::class])
    fun provideKpiDataSource(
        configInfo: ConfigInfo,
        api: KPIAPI,
        settingsPrefs: SettingsPrefs,
    ): KpiDataSource = KpiDataSourceImpl(configInfo, api, settingsPrefs)

    @Singleton
    fun provideSubmitKpiEventUseCase(kpiDataSource: KpiDataSource): SubmitKpiEventUseCase =
        SubmitKpiEventUseCase(kpiDataSource)
}