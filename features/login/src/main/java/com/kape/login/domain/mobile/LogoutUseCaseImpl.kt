package com.kape.login.domain.mobile

import com.kape.contracts.AuthenticationDataSource
import com.kape.contracts.ConnectionManager
import com.kape.contracts.LogoutUseCase
import com.kape.data.auth.ApiResult
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
import com.kape.localprefs.prefs.ShortcutPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.payments.prefs.SubscriptionPrefs
import org.koin.core.annotation.Singleton

@Singleton
class LogoutUseCaseImpl(
    private val source: AuthenticationDataSource,
    private val connectionPrefs: ConnectionPrefs,
    private val csiPrefs: CsiPrefs,
    private val customizationPrefs: CustomizationPrefs,
    private val dipPrefs: DipPrefs,
    private val networkManagementPrefs: NetworkManagementPrefs,
    private val subscriptionPrefs: SubscriptionPrefs,
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
    private val shortcutPrefs: ShortcutPrefs,
    private val vpnRegionPrefs: VpnRegionPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val kpiPrefs: KpiPrefs,
    private val consentPrefs: ConsentPrefs,
    private val ratingPrefs: RatingPrefs,
    private val connectionManager: ConnectionManager,
) : LogoutUseCase {
    override suspend fun logout(): Boolean {
        if (settingsPrefs.isAutomationEnabled.value) {
            connectionPrefs.setDisconnectedByUser(true)
        }
        if (connectionManager.isConnectionInProgress()) {
            connectionManager.disconnect().getOrNull()
        }
        return performLogout()
    }

    private suspend fun performLogout(): Boolean {
        clearPrefs()
        return when (source.logout()) {
            ApiResult.Success -> true
            is ApiResult.Error -> false
        }
    }

    private suspend fun clearPrefs() {
        connectionPrefs.clear()
        csiPrefs.clear()
        customizationPrefs.clear()
        dipPrefs.clear()
        networkManagementPrefs.clear()
        subscriptionPrefs.clear()
        shadowsocksRegionPrefs.clear()
        shortcutPrefs.clear()
        vpnRegionPrefs.clear()
        settingsPrefs.clear()
        kpiPrefs.clear()
        consentPrefs.clear()
        ratingPrefs.clear()
    }
}