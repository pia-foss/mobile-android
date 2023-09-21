package com.kape.dedicatedip.domain

import com.kape.utils.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RenewDipUseCase(private val dataSource: DipDataSource) {

    fun renew(ipToken: String): Flow<Boolean> = flow {
        dataSource.renew(ipToken).collect {
            emit(it == ApiResult.Success)
        }
    }
}