package com.kape.dedicatedip.domain

import com.kape.dedicatedip.utils.DipApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Singleton

@Singleton
class ActivateDipUseCase(private val dataSource: DipDataSource) {

    fun activate(ipToken: String): Flow<DipApiResult> = flow {
        dataSource.activate(ipToken).collect {
            emit(it)
        }
    }
}