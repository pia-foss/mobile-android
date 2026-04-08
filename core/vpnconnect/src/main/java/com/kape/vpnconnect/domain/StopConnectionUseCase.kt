package com.kape.vpnconnect.domain

import com.kape.contracts.ConnectionInfoProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.cancellation.CancellationException

internal class StopConnectionUseCase(
    private val connectionInfoProvider: ConnectionInfoProvider,
    private val connectionSource: ConnectionDataSource,
    private val stopShadowsocksUseCase: StopShadowsocksUseCase,
    private val stopPortForwardingUseCase: StopPortForwardingUseCase,
) {
    private var currentJob: Job? = null
    private val _state = MutableStateFlow<StopConnectionState>(StopConnectionState.Idle)
    val state: StateFlow<StopConnectionState> get() = _state

    /**
     * Starts the stop pipeline as a cancellable Job
     */
    fun invoke(scope: CoroutineScope): Job {
        currentJob?.cancel() // cancel any ongoing stop operation

        val job = scope.launch {
            val context = currentCoroutineContext()

            try {
                _state.value = StopConnectionState.StoppingVPN
                context.ensureActive()
                val vpnStopped = connectionSource.stopConnection()

                _state.value = StopConnectionState.ResettingInfo
                context.ensureActive()
                connectionInfoProvider.resetConnectionInfo()

                _state.value = StopConnectionState.StoppingShadowsocks
                context.ensureActive()
                stopShadowsocksUseCase()

                _state.value = StopConnectionState.StoppingPortForwarding
                context.ensureActive()
                stopPortForwardingUseCase()

                _state.value = StopConnectionState.Stopped(vpnStopped)
            } catch (e: CancellationException) {
                bestEffortCleanup()
                _state.value = StopConnectionState.Cancelled
                throw e
            } catch (e: Exception) {
                bestEffortCleanup()
                _state.value = StopConnectionState.Error(e.message ?: "Unknown error")
                throw e
            }
        }

        currentJob = job
        return job
    }

    /**
     * Cancel the ongoing stop Job
     */
    fun cancel() {
        currentJob?.cancel()
    }

    /**
     * Best-effort cleanup if cancelled or failed
     */
    private suspend fun bestEffortCleanup() {
        val context = currentCoroutineContext()
        context.ensureActive()
        try {
            stopShadowsocksUseCase()
        } catch (_: Exception) {
        }
        context.ensureActive()
        try {
            stopPortForwardingUseCase()
        } catch (_: Exception) {
        }
        context.ensureActive()
        try {
            connectionInfoProvider.resetConnectionInfo()
        } catch (_: Exception) {
        }
    }
}

/**
 * State of StopConnectionUseCase
 */
sealed class StopConnectionState {
    object Idle : StopConnectionState()
    object StoppingVPN : StopConnectionState()
    object ResettingInfo : StopConnectionState()
    object StoppingShadowsocks : StopConnectionState()
    object StoppingPortForwarding : StopConnectionState()
    data class Stopped(val vpnStopped: Boolean) : StopConnectionState()
    data class Error(val reason: String) : StopConnectionState()
    object Cancelled : StopConnectionState()
}