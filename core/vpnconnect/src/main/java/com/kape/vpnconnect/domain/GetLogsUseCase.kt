package com.kape.vpnconnect.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetLogsUseCase(private val connectionSource: ConnectionDataSource) {

    fun getDebugLogs(): Flow<List<String>> = flow {
        connectionSource.getDebugLogs().collect {
            emit(it)
        }
    }
}