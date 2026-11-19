package com.kape.vpnconnect.domain

import com.kape.contracts.ConnectionStatusSource
import com.kape.contracts.NetworkStatus
import com.kape.contracts.UsageProvider
import com.kape.data.ConnectionStatus
import com.kape.localprefs.api.AutoProtocolNudgeStore
import com.kape.localprefs.api.SettingsStore
import com.kape.settings.data.VpnProtocols
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val CONNECT_TIMEOUT = 30.seconds

private val DEAD_TUNNEL_CHECK_INTERVAL = 5.seconds

private val DEAD_TUNNEL_THRESHOLD = 60.seconds
private val IMMEDIATE_DROP_THRESHOLD = 15.seconds
private val SHORT_FAILURE_WINDOW = 10.minutes
private const val SHORT_FAILURE_WINDOW_TRIGGER_COUNT = 2
private val LONG_FAILURE_WINDOW = 24.hours
private const val LONG_FAILURE_WINDOW_TRIGGER_COUNT = 3
private val PROMPT_CAP_WINDOW = 14.days
private const val PROMPT_LIFETIME_CAP = 3
private val DISMISS_COOLDOWN = 7.days
private const val DISMISS_PERMANENT_STOP_COUNT = 2

class ConnectionProblemDetector(
    private val connectionStatusProvider: ConnectionStatusSource,
    private val hasTunnelError: () -> Boolean,
    private val settingsPrefs: SettingsStore,
    private val usageProvider: UsageProvider,
    private val networkStatus: NetworkStatus,
    private val nudgePrefs: AutoProtocolNudgeStore,
    private val scope: CoroutineScope,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private val _showNudge = MutableStateFlow(false)
    val showNudge: StateFlow<Boolean> = _showNudge.asStateFlow()

    private var connectedAtMillis: Long? = null
    private var timeoutJob: Job? = null
    private var deadTunnelJob: Job? = null

    private var nextDisconnectIsUserInitiated = false

    init {
        scope.launch {
            connectionStatusProvider.status.collectLatest { status -> handleStatus(status) }
        }
    }

    fun onProtocolNotAvailable() {
        scope.launch { recordQualifyingFailureIfEligible() }
    }

    fun onUserInitiatedDisconnect() {
        nextDisconnectIsUserInitiated = true
    }

    suspend fun onSwitchToAutomaticAccepted() {
        nudgePrefs.setState(nudgePrefs.getStateNow().copy(accepted = true))
        _showNudge.value = false
    }

    suspend fun onNudgeDismissed() {
        val current = nudgePrefs.getStateNow()
        nudgePrefs.setState(
            current.copy(dismissCount = current.dismissCount + 1, lastDismissedAt = nowMillis()),
        )
        _showNudge.value = false
    }

    private suspend fun handleStatus(status: ConnectionStatus) {
        when (status) {
            ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING -> {
                if (timeoutJob == null) {
                    timeoutJob =
                        scope.launch {
                            delay(CONNECT_TIMEOUT)
                            recordQualifyingFailureIfEligible()
                        }
                }
            }

            ConnectionStatus.CONNECTED -> {
                timeoutJob?.cancel()
                timeoutJob = null
                connectedAtMillis = nowMillis()
                startDeadTunnelMonitor()
            }

            ConnectionStatus.DISCONNECTED -> {
                timeoutJob?.cancel()
                timeoutJob = null
                deadTunnelJob?.cancel()
                deadTunnelJob = null
                val connectedAt = connectedAtMillis
                connectedAtMillis = null
                val wasUserInitiated = nextDisconnectIsUserInitiated
                nextDisconnectIsUserInitiated = false
                if (!wasUserInitiated &&
                    connectedAt != null &&
                    (nowMillis() - connectedAt).milliseconds < IMMEDIATE_DROP_THRESHOLD
                ) {
                    recordQualifyingFailureIfEligible()
                }
            }

            else -> Unit
        }
    }

    private fun startDeadTunnelMonitor() {
        deadTunnelJob?.cancel()
        deadTunnelJob =
            scope.launch {
                var last = usageProvider.tunnelTraffic.value
                var stalledSinceMillis: Long? = null
                while (isActive) {
                    delay(DEAD_TUNNEL_CHECK_INTERVAL)
                    if (connectionStatusProvider.status.value != ConnectionStatus.CONNECTED) return@launch
                    val current = usageProvider.tunnelTraffic.value
                    val received = current.received != last.received
                    val sent = current.sent != last.sent
                    last = current
                    if (received) {
                        stalledSinceMillis = null
                        continue
                    }
                    if (sent && stalledSinceMillis == null) stalledSinceMillis = nowMillis()
                    val since = stalledSinceMillis ?: continue
                    if ((nowMillis() - since).milliseconds >= DEAD_TUNNEL_THRESHOLD) {
                        stalledSinceMillis = null
                        recordQualifyingFailureIfEligible()
                    }
                }
            }
    }

    private suspend fun recordQualifyingFailureIfEligible() {
        if (settingsPrefs.getSelectedProtocolNow() == VpnProtocols.Automatic) return
        if (hasTunnelError()) return
        if (!networkStatus.isOnline.value) return

        val now = nowMillis()
        val current = nudgePrefs.getStateNow()
        if (current.accepted || current.dismissCount >= DISMISS_PERMANENT_STOP_COUNT) return
        current.lastDismissedAt?.let { if ((now - it).milliseconds < DISMISS_COOLDOWN) return }
        if (current.promptTimestamps.size >= PROMPT_LIFETIME_CAP) return
        if (current.promptTimestamps.any { (now - it).milliseconds < PROMPT_CAP_WINDOW }) return

        val recentFailures =
            (current.failureTimestamps + now).filter { (now - it).milliseconds <= LONG_FAILURE_WINDOW }
        val shortWindowCount = recentFailures.count { (now - it).milliseconds <= SHORT_FAILURE_WINDOW }
        val shouldShowNudge =
            shortWindowCount >= SHORT_FAILURE_WINDOW_TRIGGER_COUNT ||
                recentFailures.size >= LONG_FAILURE_WINDOW_TRIGGER_COUNT

        val updated =
            if (shouldShowNudge) {
                current.copy(failureTimestamps = recentFailures, promptTimestamps = current.promptTimestamps + now)
            } else {
                current.copy(failureTimestamps = recentFailures)
            }
        nudgePrefs.setState(updated)
        if (shouldShowNudge) _showNudge.value = true
    }
}