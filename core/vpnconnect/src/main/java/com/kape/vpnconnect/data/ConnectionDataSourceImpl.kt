package com.kape.vpnconnect.data

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kape.contracts.di.Qualifiers
import com.kape.data.WorkerTags
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.platformsdk.ServiceLogger
import com.kape.vpnconnect.worker.PortForwardingWorker
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import java.util.concurrent.TimeUnit

@Singleton(binds = [ConnectionDataSource::class])
class ConnectionDataSourceImpl(
    private val context: Context,
    private val accountApi: AndroidAccountAPI,
    private val connectionPrefs: ConnectionPrefs,
    private val workManager: WorkManager,
    private val settingsPrefs: SettingsPrefs,
    @Named(Qualifiers.APP_SCOPE) private val ioScope: CoroutineScope,
) : ConnectionDataSource {
    override fun getVpnToken(): String = accountApi.vpnToken() ?: ""

    override fun startPortForwarding() {
        val workRequest =
            PeriodicWorkRequestBuilder<PortForwardingWorker>(
                15,
                TimeUnit.MINUTES,
            ).build()
        workManager.enqueueUniquePeriodicWork(
            WorkerTags.PORT_FORWARDING_WORKER,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }

    override fun stopPortForwarding() {
        ioScope.launch {
            connectionPrefs.clearGateway()
            connectionPrefs.clearPortBindingInfo()
            workManager.cancelUniqueWork(WorkerTags.PORT_FORWARDING_WORKER)
        }
    }

    override suspend fun getDebugLogs(): List<String> = serviceLogger().getLogs()

    override suspend fun clearDebugLogs() = serviceLogger().clearLogs()

    private suspend fun serviceLogger(): ServiceLogger {
        val tag =
            when (settingsPrefs.getSelectedProtocolNow()) {
                VpnProtocols.WireGuard -> ServiceLogger.VpnServiceLoggerTag.WireGuard
                VpnProtocols.OpenVPN -> ServiceLogger.VpnServiceLoggerTag.OpenVpn
                VpnProtocols.Automatic -> ServiceLogger.VpnServiceLoggerTag.Automatic
            }
        return ServiceLogger(context, tag)
    }
}