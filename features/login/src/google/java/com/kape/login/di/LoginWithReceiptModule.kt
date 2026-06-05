package com.kape.login.di

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
import com.kape.login.domain.LogoutHandler
import com.kape.login.domain.mobile.BillingLogoutHandler
import com.kape.login.domain.mobile.LoginWithReceiptHandler
import com.kape.login.domain.mobile.LoginWithReceiptHandlerImpl
import com.kape.payments.prefs.SubscriptionPrefs
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class LoginWithReceiptModule {
    @Singleton(binds = [LoginWithReceiptHandler::class])
    fun provideLoginWithReceiptHandler(vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider): LoginWithReceiptHandler =
        LoginWithReceiptHandlerImpl(vpnSubscriptionPaymentProvider)

    @Singleton(binds = [LogoutHandler::class])
    fun provideLogoutHandler(
        connectionPrefs: ConnectionPrefs,
        csiPrefs: CsiPrefs,
        customizationPrefs: CustomizationPrefs,
        dipPrefs: DipPrefs,
        networkManagementPrefs: NetworkManagementPrefs,
        shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
        shortcutPrefs: ShortcutPrefs,
        vpnRegionPrefs: VpnRegionPrefs,
        settingsPrefs: SettingsPrefs,
        kpiPrefs: KpiPrefs,
        consentPrefs: ConsentPrefs,
        ratingPrefs: RatingPrefs,
        subscriptionPrefs: SubscriptionPrefs,
    ): LogoutHandler =
        BillingLogoutHandler(
            connectionPrefs,
            csiPrefs,
            customizationPrefs,
            dipPrefs,
            networkManagementPrefs,
            shadowsocksRegionPrefs,
            shortcutPrefs,
            vpnRegionPrefs,
            settingsPrefs,
            kpiPrefs,
            consentPrefs,
            ratingPrefs,
            subscriptionPrefs,
        )
}