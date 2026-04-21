package com.kape.vpn.provider

import android.app.Application
import com.privateinternetaccess.regions.PlatformInstancesProvider
import org.koin.core.annotation.Singleton

@Singleton([PlatformInstancesProvider::class])
class PlatformProvider(
    private val app: Application,
) : PlatformInstancesProvider {
    override fun provideApplicationContext(): Application = app
}