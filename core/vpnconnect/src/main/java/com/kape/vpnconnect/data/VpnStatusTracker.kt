package com.kape.vpnconnect.data

import android.content.Context
import com.kape.contracts.ConnectionStatusSource
import com.kape.contracts.di.Qualifiers
import com.kape.data.ConnectionStatus
import com.kape.platformsdk.vpn.service.models.KapeVPNConnectionStatus
import com.kape.platformsdk.vpn.service.models.KapeVpnTunnelError
import com.kape.vpnconnect.utils.NotificationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton(binds = [ConnectionStatusSource::class])
class VpnStatusTracker(
    context: Context,
    private val notificationHandler: NotificationHandler,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) : ConnectionStatusSource {
    private val titles =
        mapOf(
            ConnectionStatus.CONNECTING to context.getString(com.kape.ui.R.string.connecting),
            ConnectionStatus.CONNECTED to context.getString(com.kape.ui.R.string.vpn_protected_to_format),
            ConnectionStatus.DISCONNECTED to context.getString(com.kape.ui.R.string.vpn_not_protected),
            ConnectionStatus.DISCONNECTING to context.getString(com.kape.ui.R.string.vpn_not_protected),
            ConnectionStatus.RECONNECTING to context.getString(com.kape.ui.R.string.reconnecting),
            ConnectionStatus.PAUSED to context.getString(com.kape.ui.R.string.not_connected),
        )

    private val _status = MutableStateFlow<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    override val status: StateFlow<ConnectionStatus> = _status.asStateFlow()
    private val _title = MutableStateFlow(titles[ConnectionStatus.DISCONNECTED].orEmpty())
    override val title: StateFlow<String> = _title.asStateFlow()

    private val _lastTunnelError = MutableStateFlow<KapeVpnTunnelError?>(null)
    val lastTunnelError: StateFlow<KapeVpnTunnelError?> = _lastTunnelError.asStateFlow()

    private var connectedTimerJob: Job? = null

    fun onSdkStatusChanged(sdkStatus: KapeVPNConnectionStatus) {
        val status =
            when (sdkStatus) {
                KapeVPNConnectionStatus.Disconnecting -> ConnectionStatus.DISCONNECTING
                KapeVPNConnectionStatus.Disconnected -> ConnectionStatus.DISCONNECTED
                KapeVPNConnectionStatus.Connecting -> ConnectionStatus.CONNECTING
                KapeVPNConnectionStatus.Reconnecting -> ConnectionStatus.RECONNECTING
                KapeVPNConnectionStatus.Connected -> ConnectionStatus.CONNECTED
                KapeVPNConnectionStatus.Paused -> ConnectionStatus.PAUSED
            }
        if (status != _status.value) {
            // PiaService has already left the foreground (or been stopped) by the time we see
            // DISCONNECTED, so posting a status update here would leave an orphaned notification.
            if (status == ConnectionStatus.DISCONNECTED) {
                notificationHandler.cancel()
            } else {
                notificationHandler.update(status.toString())
            }
        }
        _status.value = status
        when (status) {
            ConnectionStatus.CONNECTED -> if (connectedTimerJob == null) startConnectedTimer()
            ConnectionStatus.DISCONNECTED -> stopConnectedTimer()
            else -> Unit
        }
        updateTitle(connectedMillis = 0)
    }

    fun onTunnelError(error: KapeVpnTunnelError?) {
        _lastTunnelError.value = error
    }

    private fun startConnectedTimer() {
        val connectedAt = System.currentTimeMillis()
        connectedTimerJob =
            appScope.launch {
                while (isActive) {
                    delay(1.seconds)
                    updateTitle(System.currentTimeMillis() - connectedAt)
                }
            }
    }

    private fun stopConnectedTimer() {
        connectedTimerJob?.cancel()
        connectedTimerJob = null
    }

    private fun updateTitle(connectedMillis: Long) {
        val current = _status.value
        val template = titles[current] ?: return
        _title.value = if (current == ConnectionStatus.CONNECTED) template.format(formatElapsed(connectedMillis)) else template.format("")
    }
}

internal fun formatElapsed(millis: Long): String {
    val totalSeconds = millis / 1_000
    return "%02d:%02d:%02d".format(totalSeconds / 3_600, (totalSeconds % 3_600) / 60, totalSeconds % 60)
}