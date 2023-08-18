package com.kape.vpnconnect.di

import android.content.Context
import com.kape.vpnconnect.R
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnconnect.data.ConnectionDataSourceImpl
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import org.koin.dsl.module

val vpnConnectModule = module {
    single<ConnectionDataSource> { ConnectionDataSourceImpl() }
    single { ConnectionUseCase(get()) }
    single { provideConnectionStatusValues(get()) }
    single { ConnectionManager(get(), get()) }
}

private fun provideConnectionStatusValues(context: Context): Map<ConnectionStatus, String> {
    val values = mutableMapOf<ConnectionStatus, String>()
    values[ConnectionStatus.CONNECTING] = context.getString(R.string.connecting)
    values[ConnectionStatus.CONNECTED] =
        context.getString(R.string.connected_to_format)
    values[ConnectionStatus.DISCONNECTED] = ""
    values[ConnectionStatus.RECONNECTING] =
        context.getString(R.string.reconnecting)
    return values
}