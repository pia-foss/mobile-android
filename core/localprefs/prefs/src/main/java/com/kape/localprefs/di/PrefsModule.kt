package com.kape.localprefs.di

import android.content.Context
import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.CustomizationPrefs
import com.kape.localprefs.prefs.DipPrefs
import com.kape.localprefs.prefs.KpiPrefs
import com.kape.localprefs.prefs.NetworkManagementPrefs
import com.kape.localprefs.prefs.RatingPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShortcutPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.payments.SubscriptionPrefs
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class PrefsModule {

    @Singleton
    fun provideSettingsPrefs(context: Context): SettingsPrefs = SettingsPrefs(context)

    @Singleton
    fun provideConnectionPrefs(context: Context): ConnectionPrefs = ConnectionPrefs(context)

    @Singleton
    fun provideCsiPrefs(context: Context): CsiPrefs = CsiPrefs(context)

    @Singleton
    fun provideRatingPrefs(context: Context): RatingPrefs = RatingPrefs(context)

    @Singleton
    fun provideKpiPrefs(context: Context): KpiPrefs = KpiPrefs(context)

    @Singleton
    fun provideConsentPrefs(context: Context): ConsentPrefs = ConsentPrefs(context)

    @Singleton
    fun provideVpnRegionPrefs(context: Context): VpnRegionPrefs = VpnRegionPrefs(context)

    @Singleton
    fun provideNetworkManagementPrefs(context: Context): NetworkManagementPrefs = NetworkManagementPrefs(context)

    @Singleton
    fun provideShortcutPrefs(context: Context): ShortcutPrefs = ShortcutPrefs(context)

    @Singleton
    fun provideShadowsocksRegionPrefs(context: Context): ShadowsocksRegionPrefs = ShadowsocksRegionPrefs(context)

    @Singleton
    fun provideDipPrefs(context: Context): DipPrefs = DipPrefs(context)

    @Singleton
    fun provideCustomizationPrefs(context: Context): CustomizationPrefs = CustomizationPrefs(context)

    @Singleton
    fun provideSubscriptionPrefs(context: Context): SubscriptionPrefs = SubscriptionPrefs(context)
}