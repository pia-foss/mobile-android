package com.kape.csi.domain

import org.koin.core.annotation.Singleton

@Singleton
class SendLogUseCase(private val dataSource: CsiDataSource) {

    suspend fun sendLog(): String = dataSource.send()
}