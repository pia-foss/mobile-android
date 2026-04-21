package com.kape.vpnconnect.di

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.work.WorkManager
import com.kape.contracts.ConfigInfo
import com.kape.contracts.ConnectionConfigurationUseCase
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.ConnectionStatusProvider
import com.kape.contracts.KpiDataSource
import com.kape.data.ConnectionStatus
import com.kape.data.DI
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.vpnconnect.data.ClientStateDataSourceImpl
import com.kape.vpnconnect.data.ConnectionDataSourceImpl
import com.kape.vpnconnect.domain.ClientStateDataSource
import com.kape.vpnconnect.domain.ConnectionConfigurationUseCaseImpl
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.ConnectionManagerImpl
import com.kape.vpnconnect.domain.GetActiveInterfaceDnsUseCase
import com.kape.vpnconnect.domain.GetActiveInterfaceDnsUseCaseImpl
import com.kape.vpnconnect.domain.GetLogsUseCase
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnconnect.utils.ConnectionInfoProviderImpl
import com.kape.vpnconnect.utils.ConnectionStatusProviderImpl
import com.kape.vpnconnect.utils.NotificationHandler
import com.kape.vpnmanager.presenters.VPNManagerAPI
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
@ComponentScan("com.kape.vpnconnect.worker")
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
            context.getString(com.kape.ui.R.string.vpn_not_protected)
        values[ConnectionStatus.RECONNECTING] =
            context.getString(com.kape.ui.R.string.reconnecting)
        return values
    }

    @Singleton
    fun provideUsageProvider(context: Context): UsageProvider = UsageProvider(context)

    @Singleton
    fun provideNotificationHandler(
        notificationManager: NotificationManager,
        notificationBuilder: Notification.Builder,
    ): NotificationHandler = NotificationHandler(notificationManager, notificationBuilder)

    @Singleton([ConnectionStatusProvider::class, VPNManagerConnectionListener::class])
    fun provideConnectionStatusProvider(
        connectionValues: Map<ConnectionStatus, String>,
        notificationHandler: NotificationHandler,
    ): ConnectionStatusProvider = ConnectionStatusProviderImpl(connectionValues, notificationHandler)

    @Singleton([ConnectionInfoProvider::class])
    fun provideConnectionInfoProvider(
        connectionStatusProvider: ConnectionStatusProvider,
        clientStateDataSource: ClientStateDataSource,
        connectionPrefs: ConnectionPrefs,
        submitKpiEventUseCase: SubmitKpiEventUseCase,
        portForwardingUseCase: PortForwardingUseCase,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
        @Named(DI.MAIN_DISPATCHER) mainDispatcher: CoroutineDispatcher,
    ): ConnectionInfoProvider =
        ConnectionInfoProviderImpl(
            connectionStatusProvider,
            clientStateDataSource,
            connectionPrefs,
            submitKpiEventUseCase,
            portForwardingUseCase,
            ioDispatcher,
            mainDispatcher,
        )

    @Singleton(binds = [GetActiveInterfaceDnsUseCase::class])
    fun provideGetActiveInterfaceDnsUseCase(context: Context): GetActiveInterfaceDnsUseCase = GetActiveInterfaceDnsUseCaseImpl(context)

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
        workManager: WorkManager,
        settingsPrefs: SettingsPrefs,
        kpiDataSource: KpiDataSource,
        usageProvider: UsageProvider,
        csiPrefs: CsiPrefs,
    ): ConnectionDataSource =
        ConnectionDataSourceImpl(
            vpnApi,
            accountApi,
            connectionPrefs,
            workManager,
            settingsPrefs,
            kpiDataSource,
            usageProvider,
            csiPrefs,
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
    ): ConnectionConfigurationUseCase =
        ConnectionConfigurationUseCaseImpl(
            connectionSource,
            configInfo.certificate,
            settingsPrefs,
            connectionPrefs,
            shadowsocksRegionPrefs,
            getActiveInterfaceDnsUseCase,
            notificationBuilder,
            configureIntent,
            automationPendingIntent,
        )

    @Singleton
    fun provideGetLogsUseCase(connectionSource: ConnectionDataSource): GetLogsUseCase = GetLogsUseCase(connectionSource)

    @Singleton([ConnectionManager::class])
    fun provideConnectionManager(): ConnectionManager = ConnectionManagerImpl()
}