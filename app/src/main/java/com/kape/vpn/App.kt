package com.kape.vpn

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.kape.appbar.di.appBarModule
import com.kape.automation.di.automationModule
import com.kape.connection.di.connectionModule
import com.kape.csi.di.csiModule
import com.kape.dedicatedip.di.dedicatedIpModule
import com.kape.login.di.loginModule
import com.kape.networkmanagement.di.networkManagementModule
import com.kape.notifications.di.notificationModule
import com.kape.payments.di.paymentsModule
import com.kape.permissions.di.permissionsModule
import com.kape.portforwarding.di.portForwardingModule
import com.kape.profile.di.profileModule
import com.kape.vpnregionselection.di.regionSelectionModule
import com.kape.settings.di.settingsModule
import com.kape.shadowsocksregions.di.shadowsocksRegionsModule
import com.kape.shareevents.di.kpiModule
import com.kape.shareevents.domain.KpiDataSource
import com.kape.sidemenu.di.sideMenuModule
import com.kape.signup.di.signupModule
import com.kape.splash.di.splashModule
import com.kape.vpn.di.appModule
import com.kape.vpn.utils.NetworkListener
import com.kape.vpnconnect.di.vpnConnectModule
import com.kape.vpnregions.di.vpnRegionsModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

class App : Application() {

    private val kpiDataSource: KpiDataSource by inject()
    private val networkListener: NetworkListener by inject()

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            kpiDataSource.flush()
            super.onStop(owner)
        }
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner
            .get()
            .lifecycle
            .addObserver(lifecycleObserver)
        setupKoinDependencyInjection()
        networkListener.register()
    }


    private fun setupKoinDependencyInjection() = startKoin {
        androidContext(this@App)
        modules(
            mutableListOf<Module>().apply {
                add(appModule)
                add(vpnConnectModule(appModule))
                add(appBarModule)
                add(notificationModule)
                add(paymentsModule(appModule))
                add(loginModule(appModule))
                add(permissionsModule(appModule))
                add(sideMenuModule(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME))
                add(profileModule(appModule))
                add(regionSelectionModule(appModule))
                add(splashModule)
                add(signupModule(appModule))
                add(kpiModule(appModule))
                add(connectionModule(appModule))
                add(
                    settingsModule(
                        appBarModule,
                        BuildConfig.VERSION_CODE,
                        BuildConfig.VERSION_NAME,
                    ),
                )
                add(portForwardingModule(appModule))
                add(dedicatedIpModule(appModule))
                add(csiModule(appModule))
                add(vpnRegionsModule(appModule))
                add(shadowsocksRegionsModule(appModule))
                add(automationModule(appModule))
                add(networkManagementModule)
            },
        )
    }
}
