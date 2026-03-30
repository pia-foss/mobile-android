package com.kape.vpnconnect.di

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import com.kape.contracts.ConfigInfo
import com.kape.contracts.KpiDataSource
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.obfuscator.domain.StartObfuscatorProcess
import com.kape.obfuscator.domain.StopObfuscatorProcess
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.utils.DI
import com.kape.vpnconnect.data.ClientStateDataSourceImpl
import com.kape.vpnconnect.data.ConnectionDataSourceImpl
import com.kape.vpnconnect.domain.ClientStateDataSource
import com.kape.vpnconnect.domain.ConnectionConfigurationUseCase
import com.kape.vpnconnect.domain.ConnectionConfigurationUseCaseImpl
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnconnect.domain.ConnectionUseCaseImpl
import com.kape.vpnconnect.domain.GetActiveInterfaceDnsUseCase
import com.kape.vpnconnect.domain.GetActiveInterfaceDnsUseCaseImpl
import com.kape.vpnconnect.domain.GetLogsUseCase
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import com.kape.vpnconnect.provider.UsageProvider
import com.privateinternetaccess.account.AndroidAccountAPI
import com.kape.vpnmanager.presenters.VPNManagerAPI
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
class VpnConnectModule {

    @Singleton
    fun provideConnectionStatusValues(context: Context): Map<ConnectionStatus, String> {
        val values = mutableMapOf<ConnectionStatus, String>()
        values[ConnectionStatus.CONNECTING] = context.getString(com.kape.ui.R.string.connecting)
        values[ConnectionStatus.CONNECTED] =
            context.getString(com.kape.ui.R.string.vpn_protected_to_format)
        values[ConnectionStatus.DISCONNECTED] =
            context.getString(com.kape.ui.R.string.vpn_not_protected)
        values[ConnectionStatus.DISCONNECTING] =
            context.getString(com.kape.ui.R.string.not_connected)
        values[ConnectionStatus.RECONNECTING] =
            context.getString(com.kape.ui.R.string.reconnecting)
        return values
    }

    @Singleton
    fun provideUsageProvider(context: Context): UsageProvider = UsageProvider(context)

    @Singleton
    fun provideConnectionManager(
        context: Context,
        connectionValues: Map<ConnectionStatus, String>,
        submitKpiEventUseCase: SubmitKpiEventUseCase,
        notificationBuilder: Notification.Builder,
    ): ConnectionManager = ConnectionManager(context, connectionValues, submitKpiEventUseCase, notificationBuilder)

    @Singleton(binds = [GetActiveInterfaceDnsUseCase::class])
    fun provideGetActiveInterfaceDnsUseCase(context: Context): GetActiveInterfaceDnsUseCase =
        GetActiveInterfaceDnsUseCaseImpl(context)

    @Singleton(binds = [ClientStateDataSource::class])
    fun provideClientStateDataSource(
        accountApi: AndroidAccountAPI,
        connectionPrefs: ConnectionPrefs,
        csiPrefs: CsiPrefs,
        settingsPrefs: SettingsPrefs,
    ): ClientStateDataSource = ClientStateDataSourceImpl(accountApi, connectionPrefs, csiPrefs, settingsPrefs)

    @Singleton(binds = [ConnectionDataSource::class])
    fun provideConnectionDataSource(
        vpnApi: VPNManagerAPI,
        accountApi: AndroidAccountAPI,
        connectionPrefs: ConnectionPrefs,
        @Named(DI.ALARM_MANAGER) alarmManager: AlarmManager,
        settingsPrefs: SettingsPrefs,
        kpiDataSource: KpiDataSource,
        usageProvider: UsageProvider,
        @Named(DI.PORT_FORWARDING_PENDING_INTENT) portForwardingIntent: PendingIntent,
        csiPrefs: CsiPrefs,
    ): ConnectionDataSource = ConnectionDataSourceImpl(
        vpnApi, accountApi, connectionPrefs, alarmManager, settingsPrefs,
        kpiDataSource, usageProvider, portForwardingIntent, csiPrefs,
    )

    @Singleton(binds = [ConnectionConfigurationUseCase::class])
    fun provideConnectionConfigurationUseCase(
        connectionSource: ConnectionDataSource,
        configInfo: ConfigInfo,
        settingsPrefs: SettingsPrefs,
        connectionPrefs: ConnectionPrefs,
        shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
        getActiveInterfaceDnsUseCase: GetActiveInterfaceDnsUseCase,
        notificationBuilder: Notification.Builder,
        configureIntent: PendingIntent,
        @Named(DI.AUTOMATION_PENDING_INTENT) automationPendingIntent: PendingIntent,
    ): ConnectionConfigurationUseCase = ConnectionConfigurationUseCaseImpl(
        connectionSource, configInfo.certificate, settingsPrefs, connectionPrefs,
        shadowsocksRegionPrefs, getActiveInterfaceDnsUseCase, notificationBuilder,
        configureIntent, automationPendingIntent,
    )

    @Singleton(binds = [ConnectionUseCase::class])
    fun provideConnectionUseCase(
        connectionSource: ConnectionDataSource,
        clientStateDataSource: ClientStateDataSource,
        connectionManager: ConnectionManager,
        connectionPrefs: ConnectionPrefs,
        settingsPrefs: SettingsPrefs,
        shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
        startObfuscatorProcess: StartObfuscatorProcess,
        stopObfuscatorProcess: StopObfuscatorProcess,
        portForwardingUseCase: PortForwardingUseCase,
        connectionConfigurationUseCase: ConnectionConfigurationUseCase,
    ): ConnectionUseCase = ConnectionUseCaseImpl(
        connectionSource, clientStateDataSource, connectionManager, connectionPrefs, settingsPrefs,
        shadowsocksRegionPrefs, startObfuscatorProcess, stopObfuscatorProcess,
        portForwardingUseCase, connectionConfigurationUseCase,
    )

    @Singleton
    fun provideGetLogsUseCase(connectionSource: ConnectionDataSource): GetLogsUseCase =
        GetLogsUseCase(connectionSource)
}