package com.kape.vpnconnect.utils

import com.kape.contracts.ConnectionStatusProvider
import com.kape.data.ConnectionStatus
import com.kape.data.DI
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
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class ConnectionStatusProviderImpl(
    private val connectionValues: Map<ConnectionStatus, String>,
    private val notificationHandler: NotificationHandler,
    @Named(DI.IO_SCOPE) private val ioScope: CoroutineScope,
) : ConnectionStatusProvider,
    VPNManagerConnectionListener {
    private var timerJob: Job? = null
    private var timer: Timer? = null
    private val _status = MutableStateFlow<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    override val status: StateFlow<ConnectionStatus> = _status.asStateFlow()
    private val _title =
        MutableStateFlow(connectionValues[ConnectionStatus.DISCONNECTED] ?: "")
    override val title: StateFlow<String> = _title.asStateFlow()
    private val _vpnManagerConnectionStatus = MutableStateFlow<VPNManagerConnectionStatus?>(null)
    override val vpnManagerConnectionStatus: StateFlow<VPNManagerConnectionStatus?> =
        _vpnManagerConnectionStatus.asStateFlow()

    override fun handleConnectionStatusChange(status: VPNManagerConnectionStatus) {
        val currentStatus =
            when (status) {
                VPNManagerConnectionStatus.Disconnecting -> ConnectionStatus.DISCONNECTING
                is VPNManagerConnectionStatus.Disconnected -> {
                    cancelTimerJob()
                    ConnectionStatus.DISCONNECTED
                }

                VPNManagerConnectionStatus.Authenticating,
                VPNManagerConnectionStatus.LinkUp,
                VPNManagerConnectionStatus.Configuring,
                VPNManagerConnectionStatus.Connecting,
                -> ConnectionStatus.CONNECTING

                VPNManagerConnectionStatus.Reconnecting -> ConnectionStatus.RECONNECTING
                is VPNManagerConnectionStatus.Connected -> {
                    if (timerJob == null) {
                        startTimer(System.currentTimeMillis())
                    }
                    ConnectionStatus.CONNECTED
                }
            }

        if (currentStatus != _status.value) {
            notificationHandler.update(currentStatus.toString())
        }
        _status.update { currentStatus }
        _vpnManagerConnectionStatus.update { status }
        setConnectionValuesTitle(timer)
    }

    private fun cancelTimerJob() {
        timerJob?.cancel()
        timerJob = null
        timer = Timer(0, 0, 0)
    }

    private fun startTimer(connectedAt: Long) {
        timerJob =
            ioScope.launch {
                while (true) {
                    timer = getTimer(System.currentTimeMillis() - connectedAt)
                    withContext(Dispatchers.Main) {
                        setConnectionValuesTitle(timer)
                    }
                    delay(1_000L.milliseconds)
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
        val current = _status.value
        connectionValues[current]?.let {
            val args =
                if (current == ConnectionStatus.CONNECTED) {
                    "%02d:%02d:%02d".format(
                        timer?.hours,
                        timer?.minutes,
                        timer?.seconds,
                    )
                } else {
                    ""
                }
            val title = String.format(it, args)
            _title.update { title }
        }
    }
}