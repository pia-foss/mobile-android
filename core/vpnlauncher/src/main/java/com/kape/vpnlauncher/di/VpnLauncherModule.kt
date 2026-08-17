package com.kape.vpnlauncher.di

import android.content.Context
import com.kape.contracts.ConnectionManager
import com.kape.vpnlauncher.VpnLauncher
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class VpnLauncherModule {
    @Singleton
    fun provideVpnLauncher(
        context: Context,
        connectionManager: ConnectionManager,
    ): VpnLauncher = VpnLauncher(context, connectionManager)
}