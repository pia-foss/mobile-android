package com.kape.csi.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Singleton

@Singleton
class SendLogUseCase(private val dataSource: CsiDataSource) {

    fun sendLog(): Flow<String> = flow {
        dataSource.send().collect {
            emit(it)
        }
    }
}