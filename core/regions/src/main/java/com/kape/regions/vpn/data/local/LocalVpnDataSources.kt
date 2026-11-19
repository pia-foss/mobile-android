package com.kape.regions.vpn.data.local

import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.DipPrefs
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse.DedicatedIPInformation
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Singleton

internal fun interface DedicatedIpDataSource {
    fun observeDedicatedIps(): Flow<List<DedicatedIPInformation>>
}

@Singleton(binds = [DedicatedIpDataSource::class])
internal class PrefsDedicatedIpDataSource(
    private val dipPrefs: DipPrefs,
) : DedicatedIpDataSource {
    override fun observeDedicatedIps(): Flow<List<DedicatedIPInformation>> = dipPrefs.dedicatedIps
}

internal fun interface SelectedVpnServerDataSource {
    fun selectedServer(): VpnServer?
}

@Singleton(binds = [SelectedVpnServerDataSource::class])
internal class PrefsSelectedVpnServerDataSource(
    private val connectionPrefs: ConnectionPrefs,
) : SelectedVpnServerDataSource {
    override fun selectedServer(): VpnServer? = connectionPrefs.selectedVpnServer.value
}