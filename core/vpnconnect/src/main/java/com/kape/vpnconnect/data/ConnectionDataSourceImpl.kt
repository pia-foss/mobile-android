package com.kape.vpnconnect.data

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kape.contracts.ConnectionManager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
) : ConnectionDataSource, KoinComponent {

    override suspend fun startConnection(
        clientConfiguration: ClientConfiguration,
        connectionStatusProvider: ConnectionStatusProvider
    ): Result<Unit> = suspendCancellableCoroutine { cont ->
        cont.invokeOnCancellation {
            CoroutineScope(cont.context).launch {
                stopConnection().getOrThrow()
            }
        }
        connectionApi.addConnectionListener(
            connectionStatusProvider as VPNManagerConnectionListener
        ) {}

        if (settingsPrefs.isHelpImprovePiaEnabled()) {
            kpiDataSource.start()
        } else {
            kpiDataSource.stop()
        }

        connectionApi.startConnection(clientConfiguration) { result ->
            result.getOrNull()?.let { serverPeerInfo ->
                connectionPrefs.setGateway(serverPeerInfo.gateway)
            } ?: run {
                csiPrefs.addCustomDebugLogs(
                    "startConnection failed: $result",
                    settingsPrefs.isDebugLoggingEnabled(),
                )
            }
            if (cont.isActive) {
                // Convert Result<ServerPeerInfo> → Result<Unit>
                cont.resume(result.map { Unit })
            }
        }
    }

    override suspend fun stopConnection(): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            connectionApi.stopConnection { result ->
                usageProvider.reset()
                stopPortForwarding()
                if (result.isFailure) {
                    csiPrefs.addCustomDebugLogs(
                        "stop connection failed: ${result.exceptionOrNull()}",
                        settingsPrefs.isDebugLoggingEnabled(),
                    )
                }
                // Resume coroutine with result
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
        }

    override fun getVpnToken(): String {
        return accountApi.vpnToken() ?: ""
    }

    override fun startPortForwarding() {
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

    override suspend fun updateConfigurationServers(servers: ServerList): Boolean =
        suspendCancellableCoroutine { cont ->
            connectionApi.updateConfigurationServers(servers) {
                cont.resume(it.isSuccess)
            }
        }
}