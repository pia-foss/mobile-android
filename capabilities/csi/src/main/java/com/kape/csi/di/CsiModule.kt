package com.kape.csi.di

import com.kape.contracts.AppInfo
import com.kape.contracts.di.Qualifiers
import com.kape.csi.data.CsiReportProviders
import com.privateinternetaccess.csi.CSIAPI
import com.privateinternetaccess.csi.CSIBuilder
import com.privateinternetaccess.csi.CSIEndpoint
import com.privateinternetaccess.csi.IEndPointProvider
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

private const val CSI_ENDPOINT = "csi.supreme.tools"
private const val CSI_TEAM_IDENTIFIER = "pia_android"
private const val REQUEST_TIMEOUT_MS = 10_000L

@Module
@ComponentScan("com.kape.csi")
class CsiModule {
    @Singleton
    fun provideCsiApi(
        appInfo: AppInfo,
        providers: CsiReportProviders,
        @Named(Qualifiers.CERTIFICATE) certificate: String,
        @Named(Qualifiers.USER_AGENT) userAgent: String,
    ): CSIAPI =
        CSIBuilder()
            .setTeamIdentifier(CSI_TEAM_IDENTIFIER)
            .setAppVersion(appInfo.versionName)
            .setCertificate(certificate)
            .setUserAgent(userAgent)
            .setEndPointProvider(
                object : IEndPointProvider {
                    override val endpoints: List<CSIEndpoint> = listOf(CSIEndpoint(CSI_ENDPOINT, false, false, null))
                },
            ).setRequestTimeoutMs(REQUEST_TIMEOUT_MS)
            .addLogProviders(*providers.all.toTypedArray())
            .build()
}