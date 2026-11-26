package com.kape.vpn

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.kape.contracts.UsageEventsControl
import com.kape.contracts.di.Qualifiers
import com.kape.csi.data.CrashRecorder
import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.location.mocklocation.MockLocationManager
import com.kape.location.mocklocation.MockLocationSync
import com.kape.vpn.di.AppModule
import com.kape.vpn.shortcuts.KeepAppShortcutsUpdatedUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.annotation.KoinApplication
import org.koin.core.qualifier.named
import org.koin.plugin.module.dsl.startKoin

@KoinApplication(modules = [AppModule::class])
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<App> {
            androidContext(this@App)
            workManagerFactory()
        }
        get<CrashRecorder>().install()
        get<MockLocationSync>().start()
        shareUsageEventsWithConsent()
        get<CoroutineScope>(named(Qualifiers.APP_SCOPE)).launch { get<KeepAppShortcutsUpdatedUseCase>()() }
        ProcessLifecycleOwner.get().lifecycle.addObserver(foregroundObserver)
    }

    private fun shareUsageEventsWithConsent() {
        val usageEvents = get<UsageEventsControl>()
        get<CoroutineScope>(named(Qualifiers.APP_SCOPE)).launch {
            get<ConsentPrefs>().allowSharing.collect { allowed -> if (allowed) usageEvents.start() else usageEvents.stop() }
        }
    }

    private val foregroundObserver =
        object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                get<MockLocationManager>().refreshMockLocationAppSelected()
            }

            override fun onStop(owner: LifecycleOwner) {
                get<UsageEventsControl>().flush()
            }
        }
}