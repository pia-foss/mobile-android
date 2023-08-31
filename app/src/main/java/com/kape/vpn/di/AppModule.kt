package com.kape.vpn.di

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.kape.connection.ConnectionPrefs
import com.kape.notifications.data.NotificationChannelManager
import com.kape.router.Router
import com.kape.settings.SettingsPrefs
import com.kape.vpn.BuildConfig
import com.kape.vpn.MainActivity
import com.kape.vpn.R
import com.kape.vpn.provider.AccountModuleStateProvider
import com.kape.vpn.provider.KPI_PREFS_NAME
import com.kape.vpn.provider.KpiModuleStateProvider
import com.kape.vpn.provider.PlatformProvider
import com.kape.vpn.provider.RegionsModuleStateProvider
import com.kape.vpn.provider.VpnManagerProvider
import com.kape.vpn.utils.VpnLauncher
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnmanager.presenters.VPNManagerAPI
import com.kape.vpnmanager.presenters.VPNManagerBuilder
import com.privateinternetaccess.account.AccountBuilder
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.Platform
import com.privateinternetaccess.kpi.KPIAPI
import com.privateinternetaccess.kpi.KPIBuilder
import com.privateinternetaccess.kpi.KPIRequestFormat
import com.privateinternetaccess.kpi.KPISendEventsMode
import com.privateinternetaccess.kpi.internals.utils.KTimeUnit
import com.privateinternetaccess.regions.RegionsAPI
import com.privateinternetaccess.regions.RegionsBuilder
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import java.io.BufferedReader

val appModule = module {
    single { provideCertificate(get()) }
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
    single { UsageProvider() }
    single { provideVpnManagerApi(get(), get(), get()) }
    single { Router() }
    single { SettingsPrefs(get()) }
    single { ConnectionPrefs(get()) }
    single { VpnLauncher(get(), get(), get()) }
}

private fun provideAndroidAccountApi(provider: AccountModuleStateProvider): AndroidAccountAPI {
    return AccountBuilder<AndroidAccountAPI>()
        .setEndpointProvider(provider)
        .setCertificate(provider.certificate)
        .setPlatform(Platform.ANDROID)
        .setUserAgentValue(provideUserAgent())
        .build()
}

private fun provideRegionsApi(
    provider: RegionsModuleStateProvider,
    platformProvider: PlatformProvider,
): RegionsAPI {
    return RegionsBuilder().setEndpointProvider(provider)
        .setCertificate(provider.certificate)
        .setUserAgent(provideUserAgent())
        .setMetadataRequestPath("/vpninfo/regions/v2")
        .setRegionsListRequestPath("/vpninfo/servers/v5")
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
        .setUserAgent(provideUserAgent())
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
        PendingIntent.FLAG_IMMUTABLE
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

private fun provideCertificate(context: Context) =
    context.assets.open("rsa4096.pem").bufferedReader().use(BufferedReader::readText)

private fun provideUserAgent() =
    "privateinternetaccess.com Android Client/${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE}))"
