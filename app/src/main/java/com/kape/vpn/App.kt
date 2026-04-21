package com.kape.vpn

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.kape.contracts.KpiDataSource
import com.kape.vpn.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

@KoinApplication(modules = [AppModule::class])
class App : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val kpiDataSource: KpiDataSource by inject()

    private val lifecycleObserver =
        object : DefaultLifecycleObserver {
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
        startKoin<App> {
            androidContext(this@App)
            workManagerFactory()
        }
    }
}