package com.kape.dedicatedip.domain

import com.kape.dedicatedip.data.models.SupportedCountries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetDipSupportedCountries(private val dataSource: DipDataSource) {

    operator fun invoke(): Flow<SupportedCountries> = flow {
        dataSource.supportedCountries().collect {
            emit(it)
        }
    }
}