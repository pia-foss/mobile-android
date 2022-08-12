package com.kape.vpn

import android.app.Application
import com.kape.connection.di.connectionModule
import com.kape.login.di.loginModule
import com.kape.payments.di.paymentsModule
import com.kape.profile.di.profileModule
import com.kape.region_selection.di.regionModule
import com.kape.share_events.di.kpiModule
import com.kape.sidemenu.di.sideMenuModule
import com.kape.signup.di.signupModule
import com.kape.splash.di.splashModule
import com.kape.vpn.di.appModule
import com.kape.vpn_permissions.di.permissionModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        setupKoinDependencyInjection()
    }

    private fun setupKoinDependencyInjection() = startKoin {
        androidContext(this@App)
        modules(mutableListOf<Module>().apply {
            add(appModule)
            add(loginModule)
            add(permissionModule)
            add(sideMenuModule(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME))
            add(profileModule)
            add(regionModule)
            add(paymentsModule)
            add(splashModule)
            add(signupModule)
            add(kpiModule)
            add(connectionModule)
        })
    }
}