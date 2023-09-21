package com.kape.dedicatedip.domain

import com.kape.utils.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ActivateDipUseCase(private val dataSource: DipDataSource) {

    fun activate(ipToken: String): Flow<Boolean> = flow {
        dataSource.activate(ipToken).collect {
            emit(it == ApiResult.Success)
        }
    }
}