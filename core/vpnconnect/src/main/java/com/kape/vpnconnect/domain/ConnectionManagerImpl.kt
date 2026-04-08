package com.kape.vpnconnect.domain

import com.kape.contracts.ConnectionManager
import com.kape.data.DI
import com.kape.data.vpnserver.VpnServer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.KoinContext
import org.koin.core.qualifier.named
import kotlin.coroutines.cancellation.CancellationException

class ConnectionManagerImpl: ConnectionManager, KoinComponent {

    private val startConnectionUseCase: StartConnectionUseCase by inject()
    private val stopConnectionUseCase: StopConnectionUseCase by inject()
    private val ioDispatcher: CoroutineDispatcher by inject(named(DI.IO_DISPATCHER))
    private val scope = CoroutineScope(ioDispatcher)

    private var currentJob: Job? = null
    private val _state = MutableStateFlow<ManagerState>(ManagerState.Idle)
    val state: StateFlow<ManagerState> get() = _state


    /**
     * Connect to VPN server
     */
    override fun connect(server: VpnServer, isManual: Boolean): Job {
        currentJob?.cancel()
        val job = scope.launch { performConnect(server, isManual) }
        currentJob = job
        return job
    }

    /**
     * Disconnect from VPN
     */
    override fun disconnect(): Job {
        currentJob?.cancel()
        _state.value = ManagerState.Disconnecting

        val job = stopConnectionUseCase.invoke(scope).also { currentJob = it }
        job.invokeOnCompletion { throwable ->
            _state.value = when {
                throwable == null -> ManagerState.Idle
                throwable is CancellationException -> ManagerState.Cancelled
                else -> ManagerState.Error(throwable.message ?: "Unknown error")
            }
        }
        return job
    }

    /**
     * Reconnect to a new VPN server.
     * Cancels any running connect and stop jobs, then starts a fresh connection.
     */
    override fun reconnect(server: VpnServer): Job {
        currentJob?.cancel() // Cancel any ongoing operation
        val job = scope.launch {
            try {
                _state.value = ManagerState.Reconnecting

                // 1️⃣ Stop existing connection if any
                val stopJob = stopConnectionUseCase.invoke(this)
                stopJob.join()

                // 2️⃣ Start new connection
                val connectJob = startConnectionUseCase.start(server, true, this)
                connectJob.join()

                // 3️⃣ Map StartConnectionUseCase state to ManagerState
                val startState = startConnectionUseCase.state.value
                _state.value = when (startState) {
                    is StartConnectionUseCase.ConnectionState.Connected -> ManagerState.Connected
                    is StartConnectionUseCase.ConnectionState.Cancelled -> ManagerState.Cancelled
                    is StartConnectionUseCase.ConnectionState.Error -> ManagerState.Error(startState.reason)
                    else -> ManagerState.Error("Unknown start state")
                }
            } catch (e: CancellationException) {
                startConnectionUseCase.cancel()
                stopConnectionUseCase.cancel()
                _state.value = ManagerState.Cancelled
                throw e
            } catch (e: Exception) {
                startConnectionUseCase.cancel()
                stopConnectionUseCase.cancel()
                _state.value = ManagerState.Error(e.message ?: "Unknown error")
            }
        }
        currentJob = job
        return job
    }

    /**
     * Cancel ongoing connect/reconnect job
     */
    fun cancelConnect() {
        currentJob?.cancel()
        startConnectionUseCase.cancel()
        stopConnectionUseCase.cancel()
    }

    /**
     * Unified state for manager
     */
    sealed class ManagerState {
        object Idle : ManagerState()
        object Connecting : ManagerState()
        object Connected : ManagerState()
        object Disconnecting : ManagerState()
        object Reconnecting : ManagerState()
        object Cancelled : ManagerState()
        data class Error(val reason: String) : ManagerState()
    }

    /**
     * Internal helper for connect
     */
    private suspend fun performConnect(server: VpnServer, isManual: Boolean) {
        _state.value = ManagerState.Connecting
        try {
            val job = startConnectionUseCase.start(server, isManual, scope)
            job.join()

            val startState = startConnectionUseCase.state.value
            _state.value = when (startState) {
                is StartConnectionUseCase.ConnectionState.Connected -> ManagerState.Connected
                is StartConnectionUseCase.ConnectionState.Cancelled -> ManagerState.Cancelled
                is StartConnectionUseCase.ConnectionState.Error -> ManagerState.Error(startState.reason)
                else -> ManagerState.Error("Unknown start state")
            }
        } catch (e: CancellationException) {
            startConnectionUseCase.cancel()
            stopConnectionUseCase.cancel()
            _state.value = ManagerState.Cancelled
            throw e
        } catch (e: Exception) {
            startConnectionUseCase.cancel()
            stopConnectionUseCase.cancel()
            _state.value = ManagerState.Error(e.message ?: "Unknown error")
        }
    }
}