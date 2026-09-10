package com.kape.vpnconnect.domain

import org.koin.core.annotation.Singleton

@Singleton
class ClearDebugLogsUseCase(
    private val connectionSource: ConnectionDataSource,
) {
    suspend fun clearDebugLogs() = connectionSource.clearDebugLogs()
}