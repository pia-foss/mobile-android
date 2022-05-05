package com.kape.vpn

import android.app.Application
import com.kape.login.di.loginModule
import com.kape.profile.di.profileModule
import com.kape.vpn.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModule, loginModule, profileModule)
        }
    }
}