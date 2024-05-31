package com.kape.login.domain.mobile

import com.kape.connection.ConnectionPrefs
import com.kape.csi.CsiPrefs
import com.kape.customization.prefs.CustomizationPrefs
import com.kape.dip.DipPrefs
import com.kape.networkmanagement.NetworkManagementPrefs
import com.kape.payments.SubscriptionPrefs
import com.kape.settings.SettingsPrefs
import com.kape.shadowsocksregions.ShadowsocksRegionPrefs
import com.kape.shareevents.KpiPrefs
import com.kape.signup.ConsentPrefs
import com.kape.utils.ApiResult
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnregions.VpnRegionPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LogoutUseCase(
    private val source: AuthenticationDataSource,
    private val connectionPrefs: ConnectionPrefs,
    private val connectionUseCase: ConnectionUseCase,
    private val csiPrefs: CsiPrefs,
    private val customizationPrefs: CustomizationPrefs,
    private val dipPrefs: DipPrefs,
    private val networkManagementPrefs: NetworkManagementPrefs,
    private val subscriptionPrefs: SubscriptionPrefs,
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
    private val vpnRegionPrefs: VpnRegionPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val kpiPrefs: KpiPrefs,
    private val consentPrefs: ConsentPrefs,
) {

    fun logout(): Flow<Boolean> = flow {
        if (settingsPrefs.isAutomationEnabled()) {
            connectionPrefs.disconnectedByUser(true)
        }
        if (connectionUseCase.isConnected()) {
            connectionUseCase.stopConnection().collect {
                performLogout().collect {
                    emit(it)
                }
            }
        } else {
            performLogout().collect {
                emit(it)
            }
        }
    }

    private fun performLogout(): Flow<Boolean> = flow {
        clearPrefs()
        source.logout().collect {
            when (it) {
                ApiResult.Success -> emit(true)
                is ApiResult.Error -> emit(false)
            }
        }
    }

    private fun clearPrefs() {
        connectionPrefs.clear()
        csiPrefs.clear()
        customizationPrefs.clear()
        dipPrefs.clear()
        networkManagementPrefs.clear()
        subscriptionPrefs.clear()
        shadowsocksRegionPrefs.clear()
        vpnRegionPrefs.clear()
        settingsPrefs.clear()
        kpiPrefs.clear()
        consentPrefs.clear()
    }
}