package com.kape.vpn.di

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.kape.appbar.di.AppBarModule
import com.kape.automation.di.AutomationModule
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.connection.di.ConnectionModule
import com.kape.contracts.AppInfo
import com.kape.contracts.ConfigInfo
import com.kape.contracts.NetworkManager
import com.kape.csi.di.CsiModule
import com.kape.customization.di.CustomizationModule
import com.kape.dedicatedip.di.DipModule
import com.kape.featureflags.di.FeatureFlagsModule
import com.kape.httpclient.data.CertificatePinningClientImpl
import com.kape.httpclient.data.GetWebsiteDownloadLinkImpl
import com.kape.httpclient.domain.CertificatePinningClient
import com.kape.httpclient.domain.GetWebsiteDownloadLink
import com.kape.localprefs.di.PrefsModule
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.RatingPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.location.di.LocationModule
import com.kape.login.di.LoginModule
import com.kape.networkmanagement.di.NetworkManagementModule
import com.kape.notifications.data.NotificationChannelManager
import com.kape.notifications.data.NotificationPermissionManager
import com.kape.obfuscationregionselection.di.ShadowsocksRegionModule
import com.kape.obfuscator.di.ObfuscatorModule
import com.kape.payments.di.PaymentsModule
import com.kape.permissions.di.PermissionsModule
import com.kape.portforwarding.data.PortForwardingApiImpl
import com.kape.portforwarding.di.PortForwardingModule
import com.kape.portforwarding.domain.PortForwardingApi
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.profile.di.ProfileModule
import com.kape.rating.utils.RatingTool
import com.kape.router.di.RouterModule
import com.kape.settings.di.SettingsModule
import com.kape.shadowsocksregions.di.ShadowsocksServersModule
import com.kape.shareevents.di.KpiModule
import com.kape.sidemenu.di.SideMenuModule
import com.kape.signup.di.SignupModule
import com.kape.snooze.di.SnoozeModule
import com.kape.splash.di.SplashModule
import com.kape.tvwelcome.di.TvWelcomeModule
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_AUTOMATION
import com.kape.ui.utils.PriceFormatter
import com.kape.utils.AutomationManager
import com.kape.utils.DI
import com.kape.utils.NetworkConnectionListener
import com.kape.utils.di.UtilsModule
import com.kape.vpn.BuildConfig
import com.kape.vpn.MainActivity
import com.kape.vpn.R
import com.kape.vpn.provider.AccountModuleStateProvider
import com.kape.vpn.provider.CSI_TEAM_IDENTIFIER
import com.kape.vpn.provider.CsiDataProvider
import com.kape.vpn.provider.CsiEndpointProvider
import com.kape.vpn.provider.KPI_PREFS_NAME
import com.kape.vpn.provider.KpiModuleStateProvider
import com.kape.vpn.provider.MetaEndpointsProvider
import com.kape.vpn.provider.RegionsModuleStateProvider
import com.kape.vpn.provider.VpnManagerProvider
import com.kape.vpn.receiver.OnRulesChangedReceiver
import com.kape.vpn.receiver.PortForwardingReceiver
import com.kape.vpn.service.AutomationService
import com.kape.vpn.service.WidgetProviderService
import com.kape.vpn.utils.USE_STAGING
import com.kape.vpnconnect.di.VpnConnectModule
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnlauncher.di.VpnLauncherModule
import com.kape.vpnmanager.presenters.VPNManagerAPI
import com.kape.vpnmanager.presenters.VPNManagerBuilder
import com.kape.vpnregionselection.di.VpnRegionModule
import com.kape.vpnregions.di.VpnServersModule
import com.privateinternetaccess.account.AccountBuilder
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.Platform
import com.privateinternetaccess.csi.CSIAPI
import com.privateinternetaccess.csi.CSIBuilder
import com.privateinternetaccess.kpi.KPIAPI
import com.privateinternetaccess.kpi.KPIBuilder
import com.privateinternetaccess.kpi.KPIRequestFormat
import com.privateinternetaccess.kpi.KPISendEventsMode
import com.privateinternetaccess.kpi.internals.utils.KTimeUnit
import com.privateinternetaccess.regions.PlatformInstancesProvider
import com.privateinternetaccess.regions.RegionsAPI
import com.privateinternetaccess.regions.RegionsBuilder
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import java.io.BufferedReader

@Module(
    includes = [
        PrefsModule::class,
        UtilsModule::class,
        RouterModule::class,
        ObfuscatorModule::class,
        KpiModule::class,
        LoginModule::class,
        SplashModule::class,
        ConnectionModule::class,
        SettingsModule::class,
        ProfileModule::class,
        SignupModule::class,
        AutomationModule::class,
        DipModule::class,
        AppBarModule::class,
        TvWelcomeModule::class,
        CustomizationModule::class,
        PermissionsModule::class,
        VpnRegionModule::class,
        ShadowsocksRegionModule::class,
        SnoozeModule::class,
        FeatureFlagsModule::class,
        CsiModule::class,
        NetworkManagementModule::class,
        LocationModule::class,
        PaymentsModule::class,
        VpnServersModule::class,
        ShadowsocksServersModule::class,
        VpnConnectModule::class,
        PortForwardingModule::class,
        VpnLauncherModule::class,
        SideMenuModule::class
    ],
)
@ComponentScan("com.kape.vpn", "com.kape.obfuscator")
class AppModule {

    @Singleton(binds = [AppInfo::class])
    fun provideAppInfo(): AppInfo = object : AppInfo {
        override val buildFlavor: String = BuildConfig.FLAVOR
        override val buildType: String = BuildConfig.BUILD_TYPE
        override val versionName: String = BuildConfig.VERSION_NAME
        override val versionCode: Int = BuildConfig.VERSION_CODE
    }

    @Singleton(binds = [ConfigInfo::class])
    fun provideConfigInfo(context: Context): ConfigInfo = object : ConfigInfo {
        override val certificate: String =
            context.assets.open("rsa4096.pem").bufferedReader().use(BufferedReader::readText)
        override val userAgent: String =
            "privateinternetaccess.com Android Client/${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE}))"
        override val updateUrl: String = BuildConfig.UPDATE_URL
        override val licences: List<String> =
            context.assets.open("acknowledgements.txt").bufferedReader()
                .use(BufferedReader::readLines)
    }

    @Singleton
    @Named("licences")
    fun provideLicences(configInfo: ConfigInfo): List<String> = configInfo.licences

    @Singleton
    @Named(DI.UPDATE_URL)
    fun provideUpdateUrl(configInfo: ConfigInfo): String = configInfo.updateUrl

    @Singleton
    fun provideBuildConfigProvider(appInfo: AppInfo): BuildConfigProvider =
        BuildConfigProvider(appInfo)

    @Singleton(binds = [CertificatePinningClient::class])
    fun provideCertificatePinningClient(configInfo: ConfigInfo): CertificatePinningClient =
        CertificatePinningClientImpl(configInfo.certificate)

    @Singleton(binds = [PortForwardingApi::class])
    fun providePortForwardingApi(
        client: CertificatePinningClient,
        configInfo: ConfigInfo,
    ): PortForwardingApi = PortForwardingApiImpl(client, configInfo.userAgent)

    @Singleton
    fun providePortForwardingUseCase(
        api: PortForwardingApi,
        connectionPrefs: ConnectionPrefs,
        settingsPrefs: SettingsPrefs,
    ): PortForwardingUseCase = PortForwardingUseCase(api, connectionPrefs, settingsPrefs)

    @Singleton(binds = [GetWebsiteDownloadLink::class])
    fun provideUpdateClient(): GetWebsiteDownloadLink = GetWebsiteDownloadLinkImpl()

    @Singleton
    fun provideAccountStateProvider(
        configInfo: ConfigInfo,
        metaEndpointsProvider: MetaEndpointsProvider,
    ): AccountModuleStateProvider =
        AccountModuleStateProvider(configInfo.certificate, metaEndpointsProvider, USE_STAGING)

    @Singleton(binds = [AndroidAccountAPI::class])
    fun provideAndroidAccountApi(
        provider: AccountModuleStateProvider,
        configInfo: ConfigInfo,
    ): AndroidAccountAPI {
        return AccountBuilder<AndroidAccountAPI>()
            .setEndpointProvider(provider)
            .setCertificate(provider.certificate)
            .setPlatform(Platform.ANDROID)
            .setUserAgentValue(configInfo.userAgent)
            .build()
    }

    @Singleton
    fun provideKpiStateProvider(
        configInfo: ConfigInfo,
        accountAPI: AndroidAccountAPI,
    ): KpiModuleStateProvider =
        KpiModuleStateProvider(configInfo.certificate, accountAPI, USE_STAGING)

    @Singleton(binds = [KPIAPI::class])
    fun provideKpiApi(provider: KpiModuleStateProvider, configInfo: ConfigInfo): KPIAPI {
        return KPIBuilder()
            .setKPIClientStateProvider(provider)
            .setKPIFlushEventMode(KPISendEventsMode.PER_BATCH)
            .setEventTimeRoundGranularity(KTimeUnit.HOURS)
            .setEventTimeSendGranularity(KTimeUnit.MILLISECONDS)
            .setRequestFormat(KPIRequestFormat.KAPE)
            .setPreferenceName(KPI_PREFS_NAME)
            .setUserAgent(configInfo.userAgent)
            .setCertificate(provider.certificate)
            .build()
    }

    @Singleton
    fun provideRegionsStateProvider(
        configInfo: ConfigInfo,
        metaEndpointsProvider: MetaEndpointsProvider,
    ): RegionsModuleStateProvider =
        RegionsModuleStateProvider(configInfo.certificate, metaEndpointsProvider, USE_STAGING)

    @Singleton([RegionsAPI::class])
    fun provideRegionsApi(
        provider: RegionsModuleStateProvider,
        platformProvider: PlatformInstancesProvider,
        configInfo: ConfigInfo,
    ): RegionsAPI {
        return RegionsBuilder()
            .setEndpointProvider(provider)
            .setCertificate(provider.certificate)
            .setUserAgent(configInfo.userAgent)
            .setMetadataRequestPath("/vpninfo/regions/v2")
            .setVpnRegionsRequestPath("/vpninfo/servers/v6")
            .setShadowsocksRegionsRequestPath("/shadow_socks")
            .setPlatformInstancesProvider(platformProvider)
            .build()
    }

    @Singleton([VPNManagerAPI::class])
    fun provideVpnManagerApi(
        context: Context,
        usageProvider: UsageProvider,
        vpnManagerProvider: VpnManagerProvider,
    ): VPNManagerAPI {
        return VPNManagerBuilder()
            .setContext(context)
            .setClientCoroutineContext(Dispatchers.Main)
            .setProtocolByteCountDependency(usageProvider)
            .setPermissionsDependency(vpnManagerProvider)
            .setDebugLoggingDependency(vpnManagerProvider)
            .build()
    }

    @Singleton
    fun provideCsiEndpoints(): CsiEndpointProvider = CsiEndpointProvider(USE_STAGING)

    @Singleton
    fun provideCsiDataProvider(
        csiPrefs: CsiPrefs,
        settingsPrefs: SettingsPrefs,
        configInfo: ConfigInfo,
    ): CsiDataProvider = CsiDataProvider(csiPrefs, settingsPrefs, configInfo.userAgent)

    @Singleton([CSIAPI::class])
    fun provideCsiApi(
        configInfo: ConfigInfo,
        endpointProvider: CsiEndpointProvider,
        csiDataProvider: CsiDataProvider,
    ): CSIAPI {
        return CSIBuilder()
            .setTeamIdentifier(CSI_TEAM_IDENTIFIER)
            .setAppVersion(BuildConfig.VERSION_NAME)
            .setCertificate(configInfo.certificate)
            .setUserAgent(configInfo.userAgent)
            .setEndPointProvider(endpointProvider)
            .addLogProviders(
                csiDataProvider.applicationInformationProvider,
                csiDataProvider.deviceInformationProvider,
                csiDataProvider.lastKnownExceptionProvider,
                csiDataProvider.protocolInformationProvider,
                csiDataProvider.regionInformationProvider,
                csiDataProvider.userSettingsProvider,
                csiDataProvider.protocolDebugLogsProvider,
                csiDataProvider.debugLogProvider,
            )
            .build()
    }

    @Singleton
    fun provideAutomationManager(
        context: Context,
        @Named(DI.AUTOMATION_SERVICE_INTENT) automationServiceIntent: Intent,
        notificationBuilder: Notification.Builder,
    ): AutomationManager = AutomationManager(context, automationServiceIntent, notificationBuilder)

    @Singleton
    fun provideNetworkConnectionListener(
        context: Context,
        networkManager: NetworkManager,
        @Named(DI.RULES_UPDATED_BROADCAST) receiver: BroadcastReceiver,
    ): NetworkConnectionListener = NetworkConnectionListener(context, networkManager, receiver)

    @Singleton
    fun provideRatingTool(
        connectionManager: ConnectionManager,
        ratingPrefs: RatingPrefs,
    ): RatingTool = RatingTool(connectionManager, ratingPrefs)

    @Singleton
    fun provideNotificationPermissionManager(context: Context): NotificationPermissionManager =
        NotificationPermissionManager(context)

    @Singleton
    fun providePriceFormatter(context: Context): PriceFormatter = PriceFormatter(context)

    @Singleton
    @Named(DI.RULES_UPDATED_INTENT)
    fun providesRulesUpdatedIntent(context: Context): Intent =
        Intent(context, OnRulesChangedReceiver::class.java)

    @Singleton
    @Named(DI.AUTOMATION_PENDING_INTENT)
    fun provideAutomationPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_AUTOMATION
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    @Singleton
    @Named(DI.PORT_FORWARDING_RECEIVER_INTENT)
    fun providePortForwardingReceiverIntent(context: Context): Intent {
        return Intent(context, PortForwardingReceiver::class.java)
    }

    @Singleton
    @Named(DI.PORT_FORWARDING_PENDING_INTENT)
    fun providePortForwardingPendingIntent(
        context: Context,
        @Named(DI.PORT_FORWARDING_RECEIVER_INTENT) intent: Intent,
    ): PendingIntent {
        val flags: Int = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    @Singleton
    @Named(DI.AUTOMATION_SERVICE_INTENT)
    fun provideAutomationServiceIntent(context: Context): Intent {
        return Intent(context, AutomationService::class.java)
    }

    @Singleton
    fun provideLauncherIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    @Singleton
    fun providePendingIntent(context: Context, intent: Intent): PendingIntent {
        return PendingIntent.getActivity(
            context,
            123,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )
    }

    @Singleton
    fun provideNotification(context: Context): Notification.Builder {
        val notificationBuilder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationBuilder(context)
            } else {
                Notification.Builder(context)
            }
        notificationBuilder.setSmallIcon(R.drawable.ic_stat_pia_robot_white)
        notificationBuilder.setCategory(Notification.CATEGORY_SERVICE)
        notificationBuilder.setOngoing(true)
        return notificationBuilder
    }

    @Singleton
    @Named(DI.WIDGET_PENDING_INTENT)
    fun provideWidgetServiceIntent(context: Context): PendingIntent {
        return PendingIntent.getService(
            context,
            0,
            Intent(context, WidgetProviderService::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    @Singleton
    @Named(DI.ALARM_MANAGER)
    fun provideAlarmManager(context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}

@RequiresApi(Build.VERSION_CODES.O)
private fun createNotificationBuilder(context: Context): Notification.Builder {
    val notificationChannel =
        NotificationChannel(
            NotificationChannelManager.CHANNEL_ID,
            NotificationChannelManager.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_MIN,
        )
    notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
    val service =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    service.createNotificationChannel(notificationChannel)
    return Notification.Builder(
        context,
        NotificationChannelManager.CHANNEL_ID,
    )
}