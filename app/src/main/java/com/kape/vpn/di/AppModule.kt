package com.kape.vpn.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kape.about.di.AboutModule
import com.kape.account.di.AccountModule
import com.kape.appbar.di.AppBarModule
import com.kape.automation.di.AutomationModule
import com.kape.buildconfig.di.BuildConfigModule
import com.kape.connection.di.ConnectionModule
import com.kape.contracts.AppInfo
import com.kape.contracts.DeviceInfo
import com.kape.contracts.di.Qualifiers
import com.kape.csi.di.CsiModule
import com.kape.dedicatedip.di.DedicatedIpModule
import com.kape.featureflags.di.FeatureFlagsModule
import com.kape.localprefs.di.PrefsModule
import com.kape.location.di.LocationModule
import com.kape.login.di.LoginModule
import com.kape.network.di.NetworkModule
import com.kape.networkmanagement.di.NetworkManagementModule
import com.kape.obfuscationregionselection.di.ObfuscationRegionSelectionModule
import com.kape.obfuscator.di.ObfuscatorModule
import com.kape.permissions.di.PermissionsModule
import com.kape.portforwarding.di.PortForwardingModule
import com.kape.profile.di.ProfileModule
import com.kape.rating.di.RatingModule
import com.kape.regions.di.RegionsModule
import com.kape.router.di.RouterModule
import com.kape.settings.di.SettingsModule
import com.kape.shareevents.di.KpiModule
import com.kape.sidemenu.di.SideMenuModule
import com.kape.signup.di.SignupModule
import com.kape.snooze.di.SnoozeModule
import com.kape.splash.di.SplashModule
import com.kape.tvwelcome.di.TvWelcomeModule
import com.kape.utils.di.UtilsModule
import com.kape.vpn.BuildConfig
import com.kape.vpn.MainActivity
import com.kape.vpnconnect.di.VpnConnectModule
import com.kape.vpnlauncher.di.VpnLauncherModule
import com.kape.vpnregionselection.di.VpnRegionSelectionModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module(
    includes = [
        UtilsModule::class,
        NetworkModule::class,
        RouterModule::class,
        PrefsModule::class,
        RegionsModule::class,
        AccountModule::class,
        FeatureFlagsModule::class,
        SplashModule::class,
        LoginModule::class,
        SignupModule::class,
        AppBarModule::class,
        DedicatedIpModule::class,
        VpnConnectModule::class,
        ObfuscatorModule::class,
        PortForwardingModule::class,
        BuildConfigModule::class,
        CsiModule::class,
        KpiModule::class,
        VpnLauncherModule::class,
        SnoozeModule::class,
        NetworkManagementModule::class,
        LocationModule::class,
        RatingModule::class,
        PermissionsModule::class,
        AboutModule::class,
        TvWelcomeModule::class,
        VpnRegionSelectionModule::class,
        ObfuscationRegionSelectionModule::class,
        ProfileModule::class,
        SideMenuModule::class,
        ConnectionModule::class,
        AutomationModule::class,
        SettingsModule::class,
    ],
)
@ComponentScan("com.kape.vpn")
class AppModule {
    @Singleton
    @Named(Qualifiers.CERTIFICATE)
    fun providePinnedCertificate(context: Context): String =
        context.assets
            .open("rsa4096.pem")
            .bufferedReader()
            .use { it.readText() }

    @Singleton
    @Named(Qualifiers.USER_AGENT)
    fun provideUserAgent(appInfo: AppInfo): String =
        "privateinternetaccess.com Android Client/${appInfo.versionName}(${appInfo.versionCode})"

    @Singleton
    @Named(Qualifiers.UPDATE_URL)
    fun provideUpdateUrl(): String = BuildConfig.UPDATE_URL

    @Singleton
    @Named(Qualifiers.VPN_NOTIFICATION_INTENT)
    fun provideVpnNotificationIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            VPN_NOTIFICATION_REQUEST_CODE,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )

    @Singleton(binds = [DeviceInfo::class])
    fun provideDeviceInfo(): DeviceInfo =
        object : DeviceInfo {
            override val manufacturer: String = Build.MANUFACTURER
            override val model: String = Build.MODEL
            override val osVersion: String = Build.VERSION.RELEASE
        }

    @Singleton(binds = [AppInfo::class])
    fun provideAppInfo(context: Context): AppInfo =
        object : AppInfo {
            override val buildFlavor: String = BuildConfig.FLAVOR
            override val buildType: String = BuildConfig.BUILD_TYPE
            override val versionName: String = BuildConfig.VERSION_NAME
            override val versionCode: Int = BuildConfig.VERSION_CODE
            override val isFreshInstall: Boolean =
                context.packageManager
                    .getPackageInfo(context.packageName, 0)
                    .let { it.firstInstallTime == it.lastUpdateTime }
        }
}

private const val VPN_NOTIFICATION_REQUEST_CODE = 123