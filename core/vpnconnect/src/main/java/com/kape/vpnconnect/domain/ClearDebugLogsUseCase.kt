package com.kape.vpnconnect.domain

import org.koin.core.annotation.Singleton
import java.util.concurrent.atomic.AtomicBoolean

@Singleton
class ClearDebugLogsUseCase(
    private val connectionSource: ConnectionDataSource,
) {
    // PiaService keeps writing log lines while it tears down after ConnectionManager.disconnect()
    // returns, so a clear issued right after disconnecting leaves those lines behind. This defers
    // a second clear to the service's own teardown, once nothing is left to log.
    private val clearScheduled = AtomicBoolean(false)

    suspend fun clearDebugLogs() = connectionSource.clearDebugLogs()

    fun scheduleClearOnSessionEnd() = clearScheduled.set(true)

    // Called when the VPN service finishes tearing down, and also when a new session starts, so a
    // schedule whose teardown never arrived is consumed then instead of wiping a later session.
    suspend fun clearIfScheduled() {
        if (clearScheduled.getAndSet(false)) clearDebugLogs()
    }
}