package com.kape.vpn.provider

import com.kape.login.BuildConfig
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.kpi.KPIClientStateProvider
import com.privateinternetaccess.kpi.KPIEndpoint

private const val KPI_ENDPOINT_PATH = "/api/client/v2/service-quality"
private const val PRODUCTION_EVENT_TOKEN = "d5fe3babe96d218323dafe20a1981e4e"
private const val STAGING_EVENT_TOKEN = "3bd9fa1b7d7ae30b6d119e335afdcfa7"

const val KPI_PREFS_NAME = "kpi-prefs"

class KpiModuleStateProvider(val certificate: String, private val accountAPI: AndroidAccountAPI) : KPIClientStateProvider {

    override fun kpiAuthToken(): String? {
        return accountAPI.apiToken()
    }

    override fun kpiEndpoints(): List<KPIEndpoint> {
        val endpoints = mutableListOf<KPIEndpoint>()
        endpoints.add(
            KPIEndpoint(
                ACCOUNT_BASE_ROOT_DOMAIN + KPI_ENDPOINT_PATH,
                usePinnedCertificate = false,
                certificateCommonName = null
            )
        )
        endpoints.add(
            KPIEndpoint(
                ACCOUNT_PROXY_ROOT_DOMAIN + KPI_ENDPOINT_PATH,
                usePinnedCertificate = false,
                certificateCommonName = null
            )
        )

        // TODO: handle staging when introduced

        return endpoints
    }

    override fun projectToken(): String {
        // TODO: handle staging vs prod, currently we use debug vs release
        return if (BuildConfig.DEBUG) {
            PRODUCTION_EVENT_TOKEN
        } else {
            STAGING_EVENT_TOKEN
        }
    }
}