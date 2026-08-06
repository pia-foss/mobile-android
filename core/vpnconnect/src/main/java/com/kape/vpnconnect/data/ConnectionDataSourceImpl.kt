package com.kape.vpnconnect.data

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kape.data.DI
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
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit

@Singleton(binds = [ConnectionDataSource::class])
class ConnectionDataSourceImpl(
    private val accountApi: AndroidAccountAPI,
    private val connectionPrefs: ConnectionPrefs,
    private val workManager: WorkManager,
    private val settingsPrefs: SettingsPrefs,
    @Named(DI.IO_SCOPE) private val ioScope: CoroutineScope,
) : ConnectionDataSource,
    KoinComponent {
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

    override suspend fun getDebugLogs(): List<String> {
        val tag =
            when (settingsPrefs.getSelectedProtocolNow()) {
                VpnProtocols.WireGuard -> ServiceLogger.VpnServiceLoggerTag.WireGuard
                VpnProtocols.OpenVPN -> ServiceLogger.VpnServiceLoggerTag.OpenVpn
            }
        return ServiceLogger(tag).getLogs()
    }
}