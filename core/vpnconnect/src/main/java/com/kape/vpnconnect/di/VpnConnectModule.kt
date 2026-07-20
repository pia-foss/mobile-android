package com.kape.vpnconnect.di

import android.app.NotificationManager
import android.content.Context
import androidx.work.WorkManager
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.ConnectionStatusProvider
import com.kape.contracts.UsageProvider
import com.kape.data.ConnectionStatus
import com.kape.data.DI
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.utils.VpnNotificationManager
import com.kape.vpnconnect.data.ClientStateDataSourceImpl
import com.kape.vpnconnect.data.ConnectionDataSourceImpl
import com.kape.vpnconnect.domain.ClientStateDataSource
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.ConnectionManagerImpl
import com.kape.vpnconnect.domain.GetActiveInterfaceDnsUseCase
import com.kape.vpnconnect.domain.GetActiveInterfaceDnsUseCaseImpl
import com.kape.vpnconnect.domain.GetLogsUseCase
import com.kape.vpnconnect.provider.UsageProviderImpl
import com.kape.vpnconnect.utils.ConnectionInfoProviderImpl
import com.kape.vpnconnect.utils.ConnectionStatusProviderImpl
import com.kape.vpnconnect.utils.NotificationHandler
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
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

    @Singleton(binds = [UsageProvider::class])
    fun provideUsageProvider(context: Context): UsageProvider = UsageProviderImpl(context)

    @Singleton
    fun provideNotificationHandler(
        notificationManager: NotificationManager,
        vpnNotificationManager: VpnNotificationManager,
    ): NotificationHandler = NotificationHandler(notificationManager, vpnNotificationManager)

    @Singleton([ConnectionStatusProvider::class])
    fun provideConnectionStatusProvider(
        connectionValues: Map<ConnectionStatus, String>,
        notificationHandler: NotificationHandler,
        @Named(DI.IO_SCOPE) ioScope: CoroutineScope,
    ): ConnectionStatusProvider =
        ConnectionStatusProviderImpl(
            connectionValues,
            notificationHandler,
            ioScope,
        )

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
        @Named(DI.IO_SCOPE) ioScope: CoroutineScope,
    ): ClientStateDataSource = ClientStateDataSourceImpl(accountApi, connectionPrefs, ioScope)

    @Singleton(binds = [ConnectionDataSource::class])
    fun provideConnectionDataSource(
        accountApi: AndroidAccountAPI,
        connectionPrefs: ConnectionPrefs,
        workManager: WorkManager,
        settingsPrefs: SettingsPrefs,
        @Named(DI.IO_SCOPE) ioScope: CoroutineScope,
    ): ConnectionDataSource =
        ConnectionDataSourceImpl(
            accountApi,
            connectionPrefs,
            workManager,
            settingsPrefs,
            ioScope,
        )

    @Singleton
    fun provideGetLogsUseCase(connectionSource: ConnectionDataSource): GetLogsUseCase = GetLogsUseCase(connectionSource)

    @Singleton([ConnectionManager::class])
    fun provideConnectionManager(): ConnectionManager = ConnectionManagerImpl()
}