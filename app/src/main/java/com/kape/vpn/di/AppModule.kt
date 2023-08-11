package com.kape.vpn.di

import android.content.Context
import com.kape.vpn.provider.AccountModuleStateProvider
import com.kape.vpn.provider.KPI_PREFS_NAME
import com.kape.vpn.provider.KpiModuleStateProvider
import com.kape.vpn.provider.RegionsModuleStateProvider
import com.kape.router.Router
import com.kape.vpn.BuildConfig
import com.privateinternetaccess.account.AccountBuilder
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.Platform
import com.privateinternetaccess.kapevpnmanager.presenters.VPNManagerAPI
import com.privateinternetaccess.kapevpnmanager.presenters.VPNManagerBuilder
import com.privateinternetaccess.kpi.KPIAPI
import com.privateinternetaccess.kpi.KPIBuilder
import com.privateinternetaccess.kpi.KPIRequestFormat
import com.privateinternetaccess.kpi.KPISendEventsMode
import com.privateinternetaccess.kpi.internals.utils.KTimeUnit
import com.privateinternetaccess.regions.RegionsAPI
import com.privateinternetaccess.regions.RegionsBuilder
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import java.io.BufferedReader

val appModule = module {
    single { provideCertificate(get()) }
    single { AccountModuleStateProvider(get()) }
    single { RegionsModuleStateProvider(get()) }
    single { KpiModuleStateProvider(get(), get()) }
    single { provideAndroidAccountApi(get()) }
    single { provideRegionsApi(get()) }
    single { provideKpiApi(get()) }
    single { provideVpnManagerApi(get()) }
    single { Router() }
}

private fun provideAndroidAccountApi(provider: AccountModuleStateProvider): AndroidAccountAPI {
    return AccountBuilder<AndroidAccountAPI>()
        .setEndpointProvider(provider)
        .setCertificate(provider.certificate)
        .setPlatform(Platform.ANDROID)
        .setUserAgentValue(provideUserAgent())
        .build()
}

private fun provideRegionsApi(provider: RegionsModuleStateProvider): RegionsAPI {
    return RegionsBuilder().setEndpointProvider(provider)
        .setCertificate(provider.certificate)
        .setUserAgent(provideUserAgent())
        .build()
}

private fun provideKpiApi(provider: KpiModuleStateProvider): KPIAPI {
    return KPIBuilder().setKPIClientStateProvider(provider)
        .setKPIFlushEventMode(KPISendEventsMode.PER_BATCH)
        .setEventTimeRoundGranularity(KTimeUnit.HOURS)
        .setEventTimeSendGranularity(KTimeUnit.MILLISECONDS)
        .setRequestFormat(KPIRequestFormat.KAPE)
        .setPreferenceName(KPI_PREFS_NAME)
        .setUserAgent(provideUserAgent())
        .setCertificate(provider.certificate)
        .build()
}

private fun provideVpnManagerApi(context: Context): VPNManagerAPI {
    return VPNManagerBuilder().setContext(context)
        .setCallbackCoroutineContext(Dispatchers.Main)
        .build()
}

private fun provideCertificate(context: Context) =
    context.assets.open("rsa4096.pem").bufferedReader().use(BufferedReader::readText)

private fun provideUserAgent() =
    "privateinternetaccess.com Android Client/${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE}))"
