package com.kape.vpn.di

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.kape.connection.ConnectionPrefs
import com.kape.csi.CsiPrefs
import com.kape.notifications.data.NotificationChannelManager
import com.kape.router.Router
import com.kape.settings.SettingsPrefs
import com.kape.vpn.BuildConfig
import com.kape.vpn.MainActivity
import com.kape.vpn.R
import com.kape.vpn.provider.AccountModuleStateProvider
import com.kape.vpn.provider.CSI_TEAM_IDENTIFIER
import com.kape.vpn.provider.CsiDataProvider
import com.kape.vpn.provider.CsiEndpointProvider
import com.kape.vpn.provider.KPI_PREFS_NAME
import com.kape.vpn.provider.KpiModuleStateProvider
import com.kape.vpn.provider.PlatformProvider
import com.kape.vpn.provider.RegionsModuleStateProvider
import com.kape.vpn.provider.VpnManagerProvider
import com.kape.vpn.receiver.OnRulesChangedReceiver
import com.kape.vpn.receiver.OnSnoozeReceiver
import com.kape.vpn.receiver.PortForwardingReceiver
import com.kape.vpn.service.WidgetProviderService
import com.kape.vpn.utils.NetworkListener
import com.kape.vpn.utils.SNOOZE_REQUEST_CODE
import com.kape.vpn.utils.SnoozeHandler
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnlauncher.VpnLauncher
import com.kape.vpnmanager.presenters.VPNManagerAPI
import com.kape.vpnmanager.presenters.VPNManagerBuilder
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
import com.privateinternetaccess.regions.RegionsAPI
import com.privateinternetaccess.regions.RegionsBuilder
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.BufferedReader

const val PARAM_USER_AGENT = "user-agent"

val appModule = module {
    single { provideCertificate(get()) }
    single(named(PARAM_USER_AGENT)) { USER_AGENT }
    single { AccountModuleStateProvider(get()) }
    single { PlatformProvider(get()) }
    single { VpnManagerProvider() }
    single { RegionsModuleStateProvider(get()) }
    single { KpiModuleStateProvider(get(), get()) }
    single { provideAndroidAccountApi(get()) }
    single { provideRegionsApi(get(), get()) }
    single { provideKpiApi(get()) }
    single { provideConfigurationIntent(get()) }
    single { providePendingIntent(get(), get()) }
    single { provideNotification(get()) }
    single(named("service-intent")) { provideWidgetServiceIntent(get()) }
    single { UsageProvider(get()) }
    single { provideVpnManagerApi(get(), get(), get()) }
    single { Router() }
    single { SettingsPrefs(get()) }
    single { ConnectionPrefs(get()) }
    single { VpnLauncher(get(), get(), get()) }
    single { provideCancelSnoozePendingIntent(get()) }
    single { provideSetSnoozePendingIntent(get()) }
    single { provideAlarmManager(get()) }
    single { SnoozeHandler(get(), get(), get(), get()) }
    single { providePortForwardingPendingIntent(get()) }
    single { CsiEndpointProvider() }
    single { CsiPrefs(get()) }
    single { CsiDataProvider(get(), get(), get(named(PARAM_USER_AGENT))) }
    single { provideCsiApi(get(), get(named(PARAM_USER_AGENT)), get(), get()) }
    single { NetworkListener(get(), get(), get(), get()) }
    single(named("rules-updated-intent")) { provideRulesUpdatedIntent(get()) }
}

private fun provideAndroidAccountApi(provider: AccountModuleStateProvider): AndroidAccountAPI {
    return AccountBuilder<AndroidAccountAPI>()
        .setEndpointProvider(provider)
        .setCertificate(provider.certificate)
        .setPlatform(Platform.ANDROID)
        .setUserAgentValue(USER_AGENT)
        .build()
}

private fun provideRegionsApi(
    provider: RegionsModuleStateProvider,
    platformProvider: PlatformProvider,
): RegionsAPI {
    return RegionsBuilder().setEndpointProvider(provider)
        .setCertificate(provider.certificate)
        .setUserAgent(USER_AGENT)
        .setMetadataRequestPath("/vpninfo/regions/v2")
        .setVpnRegionsRequestPath("/vpninfo/servers/v6")
        .setShadowsocksRegionsRequestPath("/shadow_socks")
        .setPlatformInstancesProvider(platformProvider)
        .build()
}

private fun provideKpiApi(provider: KpiModuleStateProvider): KPIAPI {
    return KPIBuilder().setKPIClientStateProvider(provider)
        .setKPIFlushEventMode(KPISendEventsMode.PER_BATCH)
        .setEventTimeRoundGranularity(KTimeUnit.HOURS)
        .setEventTimeSendGranularity(KTimeUnit.MILLISECONDS)
        .setRequestFormat(KPIRequestFormat.KAPE)
        .setPreferenceName(KPI_PREFS_NAME)
        .setUserAgent(USER_AGENT)
        .setCertificate(provider.certificate)
        .build()
}

private fun provideVpnManagerApi(
    context: Context,
    usageProvider: UsageProvider,
    vpnManagerProvider: VpnManagerProvider,
): VPNManagerAPI {
    return VPNManagerBuilder().setContext(context).setClientCoroutineContext(Dispatchers.Main)
        .setProtocolByteCountDependency(usageProvider)
        .setPermissionsDependency(vpnManagerProvider)
        .setDebugLoggingDependency(vpnManagerProvider)
        .build()
}

private fun provideCsiApi(
    certificate: String,
    userAgent: String,
    endpointProvider: CsiEndpointProvider,
    csiDataProvider: CsiDataProvider,
): CSIAPI {
    return CSIBuilder()
        .setTeamIdentifier(CSI_TEAM_IDENTIFIER)
        .setAppVersion(BuildConfig.VERSION_NAME)
        .setCertificate(certificate)
        .setUserAgent(userAgent)
        .setEndPointProvider(endpointProvider)
        .addLogProviders(
            csiDataProvider.applicationInformationProvider,
            csiDataProvider.deviceInformationProvider,
            csiDataProvider.lastKnownExceptionProvider,
            csiDataProvider.protocolInformationProvider,
            csiDataProvider.regionInformationProvider,
            csiDataProvider.userSettingsProvider,
        )
        .build()
}

private fun provideConfigurationIntent(context: Context): Intent {
    return Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

private fun providePendingIntent(context: Context, intent: Intent): PendingIntent {
    return PendingIntent.getActivity(
        context,
        123,
        intent,
        PendingIntent.FLAG_IMMUTABLE,
    )
}

private fun provideNotification(context: Context): Notification.Builder {
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

@RequiresApi(Build.VERSION_CODES.O)
private fun createNotificationBuilder(context: Context): Notification.Builder {
    val notificationChannel =
        NotificationChannel(
            NotificationChannelManager.CHANNEL_ID,
            NotificationChannelManager.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
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

private fun provideSetSnoozePendingIntent(context: Context): PendingIntent {
    return PendingIntent.getBroadcast(
        context,
        SNOOZE_REQUEST_CODE,
        Intent(context, OnSnoozeReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT,
    )
}

private fun provideCancelSnoozePendingIntent(context: Context): PendingIntent? {
    return PendingIntent.getBroadcast(
        context,
        SNOOZE_REQUEST_CODE,
        Intent(context, OnSnoozeReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
    )
}

private fun providePortForwardingPendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, PortForwardingReceiver::class.java)
    val flags: Int = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
    return PendingIntent.getBroadcast(
        context, 0, intent, flags,
    )
}

fun provideWidgetServiceIntent(context: Context): PendingIntent {
    return PendingIntent.getService(
        context,
        0,
        Intent(context, WidgetProviderService::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

fun provideRulesUpdatedIntent(context: Context): Intent {
    return Intent(context, OnRulesChangedReceiver::class.java)
}

private fun provideAlarmManager(context: Context) =
    context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

private fun provideCertificate(context: Context) =
    context.assets.open("rsa4096.pem").bufferedReader().use(BufferedReader::readText)

private const val USER_AGENT =
    "privateinternetaccess.com Android Client/${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE}))"
