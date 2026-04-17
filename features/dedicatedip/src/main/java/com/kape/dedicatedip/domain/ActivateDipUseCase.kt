package com.kape.dedicatedip.domain

import com.kape.dedicatedip.utils.DipApiResult
import org.koin.core.annotation.Singleton

@Singleton
class ActivateDipUseCase(private val dataSource: DipDataSource) {

    suspend fun activate(ipToken: String): DipApiResult = dataSource.activate(ipToken)
}