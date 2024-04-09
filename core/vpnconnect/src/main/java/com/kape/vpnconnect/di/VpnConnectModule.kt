package com.kape.vpnconnect.di

import android.content.Context
import com.kape.vpnconnect.data.ConnectionDataSourceImpl
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnconnect.domain.GetActiveInterfaceDnsUseCase
import com.kape.vpnconnect.domain.GetActiveInterfaceDnsUseCaseImpl
import com.kape.vpnconnect.domain.GetLogsUseCase
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun vpnConnectModule(appModule: Module) = module {
    includes(appModule, localVpnConnectModule)
}

private val localVpnConnectModule = module {
    single<GetActiveInterfaceDnsUseCase> { GetActiveInterfaceDnsUseCaseImpl(get()) }
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
        )
    }
    single {
        ConnectionUseCase(
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
            get(), get(), get(),
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
        context.getString(com.kape.ui.R.string.connected_to_format)
    values[ConnectionStatus.DISCONNECTED] = context.getString(com.kape.ui.R.string.not_connected)
    values[ConnectionStatus.DISCONNECTING] = context.getString(com.kape.ui.R.string.not_connected)
    values[ConnectionStatus.RECONNECTING] =
        context.getString(com.kape.ui.R.string.reconnecting)
    return values
}