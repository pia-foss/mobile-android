package com.kape.csi.domain

import org.koin.core.annotation.Factory

fun interface DiagnosticsDataSource {
    suspend fun send(): String?
}

@Factory
class SendDiagnosticsUseCase(
    private val dataSource: DiagnosticsDataSource,
) {
    suspend operator fun invoke(): String? = dataSource.send()
}