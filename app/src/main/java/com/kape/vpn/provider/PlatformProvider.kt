package com.kape.vpn.provider

import android.app.Application
import com.privateinternetaccess.regions.PlatformInstancesProvider

class PlatformProvider(private val app: Application) : PlatformInstancesProvider {
    override fun provideApplicationContext(): Application {
        return app
    }
}