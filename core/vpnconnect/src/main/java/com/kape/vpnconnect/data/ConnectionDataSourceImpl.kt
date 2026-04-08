package com.kape.vpnconnect.data

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kape.contracts.ConnectionStatusProvider
import com.kape.contracts.KpiDataSource
import com.kape.data.WorkerTags
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnconnect.worker.PortForwardingWorker
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.presenters.VPNManagerAPI
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

@Singleton(binds = [ConnectionDataSource::class])
class ConnectionDataSourceImpl(
    private val connectionApi: VPNManagerAPI,
    private val accountApi: AndroidAccountAPI,
    private val connectionPrefs: ConnectionPrefs,
    private val workManager: WorkManager,
    private val settingsPrefs: SettingsPrefs,
    private val kpiDataSource: KpiDataSource,
    private val usageProvider: UsageProvider,
    private val csiPrefs: CsiPrefs,
    private val connectionStatusProvider: ConnectionStatusProvider
) : ConnectionDataSource, KoinComponent {

    init {
        connectionApi.addConnectionListener(connectionStatusProvider as VPNManagerConnectionListener) {}
    }

    override suspend fun startConnection(
        clientConfiguration: ClientConfiguration,
    ): Boolean = suspendCancellableCoroutine { cont ->
        if (settingsPrefs.isHelpImprovePiaEnabled()) {
            kpiDataSource.start()
        } else {
            kpiDataSource.stop()
        }

        // Handle cancellation
        cont.invokeOnCancellation {
            connectionApi.stopConnection(){} // 👈 IMPORTANT (if available)
        }

        connectionApi.startConnection(clientConfiguration) { result ->

            if (!cont.isActive) return@startConnection // 👈 avoid resume after cancel

            result.getOrNull()?.let { serverPeerInfo ->
                connectionPrefs.setGateway(serverPeerInfo.gateway)
            } ?: run {
                csiPrefs.addCustomDebugLogs(
                    "startConnection failed: $result",
                    settingsPrefs.isDebugLoggingEnabled(),
                )
            }

            cont.resume(result.isSuccess)
        }
    }

    override suspend fun stopConnection(): Boolean = suspendCancellableCoroutine { cont ->
        connectionApi.stopConnection {
            usageProvider.reset()
            stopPortForwarding()
            cont.resume(it.isSuccess)
            if (it.isFailure) {
                csiPrefs.addCustomDebugLogs(
                    "stop connection failed: ${it.exceptionOrNull()}",
                    settingsPrefs.isDebugLoggingEnabled(),
                )
            }
        }
    }

    override fun getVpnToken(): String {
        return accountApi.vpnToken() ?: ""
    }

    override fun startPortForwarding() {
        // TODO: handle how to pass status
        val workRequest = PeriodicWorkRequestBuilder<PortForwardingWorker>(
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
        // TODO: handle how to pass status
        connectionPrefs.clearGateway()
        connectionPrefs.clearPortBindingInfo()
        workManager.cancelUniqueWork(WorkerTags.PORT_FORWARDING_WORKER)
    }

    override suspend fun getDebugLogs(): List<String> = suspendCancellableCoroutine { cont ->
        val target = when (settingsPrefs.getSelectedProtocol()) {
            VpnProtocols.WireGuard -> VPNManagerProtocolTarget.WIREGUARD
            VpnProtocols.OpenVPN -> VPNManagerProtocolTarget.OPENVPN
        }
        connectionApi.getVpnProtocolLogs(target) {
            if (it.isSuccess) {
                cont.resume(it.getOrDefault(emptyList()))
            } else {
                cont.resume(emptyList())
            }
        }
    }

    override suspend fun updateConfigurationServers(servers: ServerList): Boolean = suspendCancellableCoroutine { cont ->
        connectionApi.updateConfigurationServers(servers) {
            cont.resume(it.isSuccess)
        }
    }
}