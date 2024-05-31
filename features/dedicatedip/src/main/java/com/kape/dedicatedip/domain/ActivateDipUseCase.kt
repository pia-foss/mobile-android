package com.kape.dedicatedip.domain

import com.kape.dedicatedip.utils.DipApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ActivateDipUseCase(private val dataSource: DipDataSource) {

    fun activate(ipToken: String): Flow<DipApiResult> = flow {
        dataSource.activate(ipToken).collect {
            emit(it)
        }
    }
}