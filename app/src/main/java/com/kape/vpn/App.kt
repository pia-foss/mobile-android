package com.kape.vpn

import android.app.Application
import com.kape.login.di.loginModule
import com.kape.sidemenu.di.sideMenuModule
import com.kape.profile.di.profileModule
import com.kape.vpn_permissions.di.permissionModule
import com.kape.vpn.di.appModule
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
            add(sideMenuModule)
            add(profileModule)
        })
    }
}