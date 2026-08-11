package com.kape.shareevents.di

import com.kape.contracts.AppInfo
import com.kape.contracts.ConfigInfo
import com.kape.contracts.KpiDataSource
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.shareevents.data.KpiDataSourceImpl
import com.kape.shareevents.data.KpiEventGenerator
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.privateinternetaccess.kpi.KPIAPI
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class KpiModule {
    @Singleton
    fun provideKpiEventGenerator(
        appInfo: AppInfo,
        configInfo: ConfigInfo,
        settingsPrefs: SettingsPrefs,
    ): KpiEventGenerator = KpiEventGenerator(appInfo, configInfo, settingsPrefs)

    @Singleton(binds = [KpiDataSource::class])
    fun provideKpiDataSource(
        api: KPIAPI,
        eventGenerator: KpiEventGenerator,
    ): KpiDataSource = KpiDataSourceImpl(api, eventGenerator)

    @Singleton
    fun provideSubmitKpiEventUseCase(kpiDataSource: KpiDataSource): SubmitKpiEventUseCase = SubmitKpiEventUseCase(kpiDataSource)
}