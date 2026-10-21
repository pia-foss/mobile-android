package com.kape.vpn

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.KpiDataSource
import com.kape.data.ConnectionStatus
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.location.mocklocation.MockLocationManager
import com.kape.vpn.di.AppModule
import com.kape.widget.WidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

@KoinApplication(modules = [AppModule::class])
class App : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val kpiDataSource: KpiDataSource by inject()
    private val connectionInfoProvider: ConnectionInfoProvider by inject()
    private val consentPrefs: ConsentPrefs by inject()
    private val csiPrefs: CsiPrefs by inject()
    private val settingsPrefs: SettingsPrefs by inject()
    private val connectionPrefs: ConnectionPrefs by inject()
    private val mockLocationManager: MockLocationManager by inject()

    private val lifecycleObserver =
        object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                // The mock location app can be changed in Developer options while we're in the background.
                mockLocationManager.refreshMockLocationAppSelected()
            }

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
        observeConnectionStateForWidget()
        observeConnectionStateForMockLocation()
        appScope.launch {
            consentPrefs.allowSharing.collectLatest {
                when (it) {
                    true -> kpiDataSource.start()
                    false -> kpiDataSource.stop()
                }
            }
        }

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runBlocking {
                csiPrefs.setLastKnownException(throwable.stackTraceToString())
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun observeConnectionStateForWidget() {
        appScope.launch {
            connectionInfoProvider.status
                .distinctUntilChangedBy { it }
                .collect { refreshWidget() }
        }
    }

    // While enabled, PIA is the selected mock location app and the VPN is connected, the device
    // location is mocked to the connected server's coordinates.
    private fun observeConnectionStateForMockLocation() {
        appScope.launch {
            combine(
                settingsPrefs.isMockLocationEnabled,
                mockLocationManager.mockLocationAppSelected,
                connectionInfoProvider.status,
                connectionPrefs.selectedVpnServer,
            ) { enabled, appSelected, status, server ->
                val latitude = server?.latitude?.toDoubleOrNull()
                val longitude = server?.longitude?.toDoubleOrNull()
                if (enabled && appSelected && status == ConnectionStatus.CONNECTED && latitude != null && longitude != null) {
                    latitude to longitude
                } else {
                    null
                }
            }.distinctUntilChanged()
                .collect { coordinates ->
                    if (coordinates != null) {
                        mockLocationManager.start(coordinates.first, coordinates.second)
                    } else {
                        mockLocationManager.stop()
                    }
                }
        }
    }

    private fun refreshWidget() {
        val manager = AppWidgetManager.getInstance(this)
        val ids = manager.getAppWidgetIds(ComponentName(this, WidgetReceiver::class.java))
        if (ids.isNotEmpty()) {
            sendBroadcast(
                Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    component = ComponentName(this@App, WidgetReceiver::class.java)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                },
            )
        }
    }
}