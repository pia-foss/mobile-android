package com.kape.vpnconnect.utils

import com.kape.contracts.ConnectionStatusProvider
import com.kape.data.ConnectionStatus
import com.kape.data.VpnConnectionStatus
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Singleton

@Singleton
class ConnectionStatusProviderImpl(
    private val connectionValues: Map<ConnectionStatus, String>,
    private val notificationHandler: NotificationHandler,
) : ConnectionStatusProvider, VPNManagerConnectionListener {
    private var timerJob: Job? = null
    private var timer: Timer? = null
    private val defaultVpnConnectionStatus = VpnConnectionStatus(
        ConnectionStatus.DISCONNECTED,
        connectionValues[ConnectionStatus.DISCONNECTED] ?: "",
    )
    private val _state: MutableStateFlow<VpnConnectionStatus> =
        MutableStateFlow(defaultVpnConnectionStatus)
    override val state: StateFlow<VpnConnectionStatus> = _state.asStateFlow()

    override fun handleConnectionStatusChange(status: VPNManagerConnectionStatus) {
        println("--- handleConnectionStatusChange: $status")
        val currentStatus = when (status) {
            VPNManagerConnectionStatus.Disconnecting -> {
                cancelTimerJob()
                ConnectionStatus.DISCONNECTING
            }

            is VPNManagerConnectionStatus.Disconnected -> ConnectionStatus.DISCONNECTED
            VPNManagerConnectionStatus.Authenticating,
            VPNManagerConnectionStatus.LinkUp,
            VPNManagerConnectionStatus.Configuring,
            VPNManagerConnectionStatus.Connecting,
                -> ConnectionStatus.CONNECTING

            VPNManagerConnectionStatus.Reconnecting -> ConnectionStatus.RECONNECTING
            is VPNManagerConnectionStatus.Connected -> {
                cancelTimerJob()
                startTimer(System.currentTimeMillis())
                ConnectionStatus.CONNECTED
            }
        }

        if (currentStatus != _state.value.status) {
            notificationHandler.update(currentStatus.toString())
        }
        setConnectionValuesTitle(timer)
        _state.update { it.copy(status = currentStatus, vpnManagerConnectionStatus = status) }
    }

    private fun cancelTimerJob() {
        timerJob?.cancel()
        timerJob = null
        timer = Timer(0, 0, 0)
    }

    private fun startTimer(connectedAt: Long) {
        timerJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(1_000L)
                timer = getTimer(System.currentTimeMillis() - connectedAt)
                withContext(Dispatchers.Main) {
                    setConnectionValuesTitle(timer)
                }
            }
        }
    }

    private fun getTimer(timeConnected: Long): Timer {
        var remainingSeconds = timeConnected
        val hours = remainingSeconds / 3_600_000
        remainingSeconds %= 3_600_000
        val minutes = remainingSeconds / 60_000
        remainingSeconds %= 60_000
        val seconds = remainingSeconds / 1_000
        return Timer(hours, minutes, seconds)
    }

    private fun setConnectionValuesTitle(timer: Timer?) {
        val status = _state.value.status
        connectionValues[status]?.let {
            val args = if (status == ConnectionStatus.CONNECTED) {
                "%02d:%02d:%02d".format(
                    timer?.hours,
                    timer?.minutes,
                    timer?.seconds,
                )
            } else {
                ""
            }
            val title = String.format(it, args)
            _state.update { it.copy(status, title) }
        }
    }
}