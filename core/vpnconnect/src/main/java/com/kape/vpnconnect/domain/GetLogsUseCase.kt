package com.kape.vpnconnect.domain

import org.koin.core.annotation.Singleton

@Singleton
class GetLogsUseCase(private val connectionSource: ConnectionDataSource) {

    suspend fun getDebugLogs(): List<String> = connectionSource.getDebugLogs()
}