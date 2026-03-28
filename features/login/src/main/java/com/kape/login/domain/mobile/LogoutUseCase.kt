package com.kape.login.domain.mobile

import com.kape.contracts.AuthenticationDataSource
import com.kape.contracts.data.auth.ApiResult
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.CustomizationPrefs
import com.kape.localprefs.prefs.DipPrefs
import com.kape.localprefs.prefs.KpiPrefs
import com.kape.localprefs.prefs.NetworkManagementPrefs
import com.kape.localprefs.prefs.RatingPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.payments.SubscriptionPrefs
import com.kape.vpnconnect.domain.ConnectionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Singleton

@Singleton
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
    private val ratingPrefs: RatingPrefs,
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
        ratingPrefs.clear()
    }
}