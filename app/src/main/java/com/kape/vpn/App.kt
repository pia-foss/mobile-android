package com.kape.vpn

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.KpiDataSource
import com.kape.vpn.di.AppModule
import com.kape.widget.WidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
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

    private val lifecycleObserver =
        object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                kpiDataSource.flush()
                super.onStop(owner)
            }
        }

    override fun onCreate() {
        super.onCreate()
        recoverCorruptedAccountPrefs()
        ProcessLifecycleOwner
            .get()
            .lifecycle
            .addObserver(lifecycleObserver)
        startKoin<App> {
            androidContext(this@App)
            workManagerFactory()
        }
        observeConnectionStateForWidget()
    }

    // The account library stores credentials in EncryptedSharedPreferences. If the app is
    // reinstalled or restored from backup the Keystore master key changes but the encrypted
    // file survives, causing AEADBadTagException inside the library's static initializer —
    // which permanently poisons the class and crashes the process. Probing the file here
    // before Koin starts lets us delete it if the current Keystore key can't open it.
    // The user is logged out, but the app no longer crashes.
    private fun recoverCorruptedAccountPrefs() {
        val prefsName = "account_shared_preferences"
        try {
            val masterKey =
                MasterKey
                    .Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .setUserAuthenticationRequired(false)
                    .build()
            EncryptedSharedPreferences.create(
                this,
                prefsName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (e: Exception) {
            deleteSharedPreferences(prefsName)
        }
    }

    private fun observeConnectionStateForWidget() {
        appScope.launch {
            connectionInfoProvider.status
                .distinctUntilChangedBy { it }
                .collect { refreshWidget() }
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