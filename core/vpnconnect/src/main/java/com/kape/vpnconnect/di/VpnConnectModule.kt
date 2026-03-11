package com.kape.vpnconnect.di

import android.content.Context
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
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun vpnConnectModule(appModule: Module) = module {
    includes(appModule, internalVpnConnectModule)
}

internal val internalVpnConnectModule = module {
    single<GetActiveInterfaceDnsUseCase> { GetActiveInterfaceDnsUseCaseImpl(get()) }
    single<ClientStateDataSource> { ClientStateDataSourceImpl(get(), get(), get(), get()) }
    single<ConnectionDataSource> {
        ConnectionDataSourceImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(named("port-forwarding-pending-intent")),
            get(),
        )
    }
    single<ConnectionUseCase> {
        ConnectionUseCaseImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    single<ConnectionConfigurationUseCase> {
        ConnectionConfigurationUseCaseImpl(
            get(),
            get(named("certificate")),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(named("automation-pending-intent")),
        )
    }
    single { provideConnectionStatusValues(get()) }
    single { ConnectionManager(get(), get(), get(), get()) }
    single { GetLogsUseCase(get()) }
}

private fun provideConnectionStatusValues(context: Context): Map<ConnectionStatus, String> {
    val values = mutableMapOf<ConnectionStatus, String>()
    values[ConnectionStatus.CONNECTING] = context.getString(com.kape.ui.R.string.connecting)
    values[ConnectionStatus.CONNECTED] =
        context.getString(com.kape.ui.R.string.vpn_protected_to_format)
    values[ConnectionStatus.DISCONNECTED] =
        context.getString(com.kape.ui.R.string.vpn_not_protected)
    values[ConnectionStatus.DISCONNECTING] = context.getString(com.kape.ui.R.string.not_connected)
    values[ConnectionStatus.RECONNECTING] =
        context.getString(com.kape.ui.R.string.reconnecting)
    return values
}