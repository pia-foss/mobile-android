package com.kape.vpnconnect.utils

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import com.kape.shareevents.data.models.KpiConnectionStatus
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val NOTIFICATION_ID = 123

class ConnectionManager(
    private val context: Context,
    private val connectionValues: Map<ConnectionStatus, String>,
    private val submitKpiEventUseCase: SubmitKpiEventUseCase,
    private val notificationBuilder: Notification.Builder,
) : VPNManagerConnectionListener {
    private val _connectionStatus =
        MutableStateFlow<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus
    private var timerJob: Job? = null
    private var timer: Timer? = null
    private val _serverName = MutableStateFlow("")
    val serverName: StateFlow<String> = _serverName

    private val _serverIso = MutableStateFlow("")
    val serverIso: StateFlow<String> = _serverIso

    private val _connectionStatusTitle =
        MutableStateFlow(context.getString(com.kape.ui.R.string.vpn_not_protected))
    val connectionStatusTitle: StateFlow<String> = _connectionStatusTitle

    var isManualConnection: Boolean = false

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun isConnected(): Boolean = connectionStatus.value == ConnectionStatus.CONNECTED

    fun isConnecting(): Boolean =
        connectionStatus.value == ConnectionStatus.CONNECTING || connectionStatus.value == ConnectionStatus.RECONNECTING

    fun setConnectedServerName(serverName: String, iso: String) {
        _serverName.value = serverName
        _serverIso.value = iso
    }

    override fun handleConnectionStatusChange(status: VPNManagerConnectionStatus) {
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
        // If we update the notification too often, Android will silence it.
        if (_connectionStatus.value != currentStatus) {
            notificationBuilder.setContentText(currentStatus.toString())
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
        _connectionStatus.value = currentStatus
        setConnectionValuesTitle(timer)
        submitKpiEventUseCase.submitConnectionEvent(
            getKpiConnectionStatus(status),
            isManualConnection,
        )
    }

    private fun getKpiConnectionStatus(status: VPNManagerConnectionStatus): KpiConnectionStatus {
        return when (status) {
            VPNManagerConnectionStatus.Disconnecting,
            is VPNManagerConnectionStatus.Disconnected,
            -> KpiConnectionStatus.NotConnected

            VPNManagerConnectionStatus.Authenticating,
            VPNManagerConnectionStatus.LinkUp,
            VPNManagerConnectionStatus.Configuring,
            VPNManagerConnectionStatus.Connecting,
            -> KpiConnectionStatus.Connecting

            VPNManagerConnectionStatus.Reconnecting -> KpiConnectionStatus.Reconnecting
            is VPNManagerConnectionStatus.Connected -> KpiConnectionStatus.Connected
        }
    }

    private fun setConnectionValuesTitle(timer: Timer?) {
        val status = _connectionStatus.value
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
            _connectionStatusTitle.value = String.format(it, args)
        }
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
}