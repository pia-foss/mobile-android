package com.kape.vpnconnect.data

import com.kape.contracts.DebugLogCleaner
import com.kape.vpnconnect.domain.ConnectionDataSource
import org.koin.core.annotation.Singleton
import java.util.concurrent.atomic.AtomicBoolean

@Singleton(binds = [DebugLogCleaner::class])
class DebugLogCleanerImpl(
    private val connectionSource: ConnectionDataSource,
) : DebugLogCleaner {
    // PiaService keeps writing log lines while it tears down after ConnectionManager.disconnect()
    // returns, so a clear issued right after disconnecting leaves those lines behind. This defers
    // a second clear to the service's own teardown, once nothing is left to log.
    private val clearScheduled = AtomicBoolean(false)

    override suspend fun clearDebugLogs() = connectionSource.clearDebugLogs()

    override fun scheduleClearOnSessionEnd() = clearScheduled.set(true)

    suspend fun clearIfScheduled() {
        if (clearScheduled.getAndSet(false)) clearDebugLogs()
    }
}