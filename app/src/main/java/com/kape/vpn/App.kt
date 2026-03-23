package com.kape.vpn

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.kape.appbar.di.appBarModule
import com.kape.automation.di.automationModule
import com.kape.buildconfig.di.buildConfigModule
import com.kape.connection.di.connectionModule
import com.kape.csi.di.csiModule
import com.kape.customization.di.customizationModule
import com.kape.dedicatedip.di.dedicatedIpModule
import com.kape.featureflags.di.featureFlagsModule
import com.kape.login.di.loginModule
import com.kape.networkmanagement.di.networkManagementModule
import com.kape.notifications.di.notificationModule
import com.kape.obfuscationregionselection.di.shadowsocksSelectionModule
import com.kape.obfuscator.di.obfuscatorModule
import com.kape.payments.di.paymentsModule
import com.kape.permissions.di.permissionsModule
import com.kape.portforwarding.di.portForwardingModule
import com.kape.profile.di.profileModule
import com.kape.settings.di.settingsModule
import com.kape.shadowsocksregions.di.shadowsocksRegionsModule
import com.kape.shareevents.di.kpiModule
import com.kape.shareevents.domain.KpiDataSource
import com.kape.sidemenu.di.sideMenuModule
import com.kape.signup.di.signupModule
import com.kape.snooze.di.snoozeModule
import com.kape.splash.di.splashModule
import com.kape.tvwelcome.di.tvWelcomeModule
import com.kape.vpn.di.appModule
import com.kape.vpnconnect.di.vpnConnectModule
import com.kape.vpnregions.di.vpnRegionsModule
import com.kape.vpnregionselection.di.regionSelectionModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class App : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val kpiDataSource: KpiDataSource by inject()

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
        loadDeferredModules()
    }

    private fun setupKoinDependencyInjection() = startKoin {
        androidContext(this@App)
        modules(
            appModule,
            vpnConnectModule(appModule),
            appBarModule(appModule),
            notificationModule,
            buildConfigModule(BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE),
            loginModule(appModule),
            splashModule,
            kpiModule(appModule),
            connectionModule(appModule),
            vpnRegionsModule(appModule),
            networkManagementModule,
            featureFlagsModule,
        )
    }

    private fun loadDeferredModules() {
        appScope.launch {
            GlobalContext.get().loadModules(
                listOf(
                    paymentsModule(appModule),
                    permissionsModule(appModule),
                    sideMenuModule(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME),
                    profileModule(appModule),
                    regionSelectionModule(appModule),
                    shadowsocksSelectionModule(appModule),
                    tvWelcomeModule,
                    signupModule(appModule),
                    settingsModule(
                        appBarModule(appModule),
                        BuildConfig.VERSION_CODE,
                        BuildConfig.VERSION_NAME,
                    ),
                    portForwardingModule(appModule),
                    dedicatedIpModule(appModule),
                    csiModule(appModule),
                    shadowsocksRegionsModule(appModule),
                    automationModule(appModule),
                    customizationModule(appModule),
                    snoozeModule(appModule),
                    obfuscatorModule(appModule),
                ),
            )
        }
    }
}
