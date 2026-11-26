package com.kape.shareevents.di

import com.kape.contracts.di.Qualifiers
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.kpi.KPIAPI
import com.privateinternetaccess.kpi.KPIBuilder
import com.privateinternetaccess.kpi.KPIClientStateProvider
import com.privateinternetaccess.kpi.KPIEndpoint
import com.privateinternetaccess.kpi.KPIRequestFormat
import com.privateinternetaccess.kpi.KPISendEventsMode
import com.privateinternetaccess.kpi.internals.utils.KTimeUnit
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

private const val KPI_ENDPOINT_PATH = "/api/client/v2/service-quality"
private const val ACCOUNT_BASE_ROOT_DOMAIN = "privateinternetaccess.com"
private const val ACCOUNT_PROXY_ROOT_DOMAIN = "piaproxy.net"
private const val PRODUCTION_EVENT_TOKEN = "d5fe3babe96d218323dafe20a1981e4e"

// Kept from mobile-android, so events queued by the old app are still sent.
private const val KPI_PREFS_NAME = "kpi-prefs"

@Module
@ComponentScan("com.kape.shareevents")
class KpiModule {
    @Singleton
    fun provideKpiApi(
        accountApi: AndroidAccountAPI,
        @Named(Qualifiers.CERTIFICATE) certificate: String,
        @Named(Qualifiers.USER_AGENT) userAgent: String,
    ): KPIAPI =
        KPIBuilder()
            .setKPIClientStateProvider(
                object : KPIClientStateProvider {
                    override fun kpiAuthToken(): String? = accountApi.apiToken()

                    override fun kpiEndpoints(): List<KPIEndpoint> =
                        listOf(ACCOUNT_BASE_ROOT_DOMAIN, ACCOUNT_PROXY_ROOT_DOMAIN).map {
                            KPIEndpoint(it + KPI_ENDPOINT_PATH, usePinnedCertificate = false, certificateCommonName = null)
                        }

                    override fun projectToken(): String = PRODUCTION_EVENT_TOKEN
                },
            ).setKPIFlushEventMode(KPISendEventsMode.PER_BATCH)
            .setEventTimeRoundGranularity(KTimeUnit.HOURS)
            .setEventTimeSendGranularity(KTimeUnit.MILLISECONDS)
            .setRequestFormat(KPIRequestFormat.KAPE)
            .setPreferenceName(KPI_PREFS_NAME)
            .setUserAgent(userAgent)
            .setCertificate(certificate)
            .build()
}