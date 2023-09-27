package com.kape.dedicatedip.domain

import com.kape.dedicatedip.utils.DipApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RenewDipUseCase(private val dataSource: DipDataSource) {

    fun renew(ipToken: String): Flow<DipApiResult> = flow {
        dataSource.renew(ipToken).collect {
            emit(it)
        }
    }
}