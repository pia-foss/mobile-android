package com.kape.dedicatedip.domain

import com.kape.dedicatedip.utils.DipApiResult
import org.koin.core.annotation.Singleton

@Singleton
class RenewDipUseCase(
    private val dataSource: DipDataSource,
) {
    suspend fun renew(ipToken: String): DipApiResult = dataSource.renew(ipToken)
}