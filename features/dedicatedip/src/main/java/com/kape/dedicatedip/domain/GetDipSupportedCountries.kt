package com.kape.dedicatedip.domain

import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetDipSupportedCountries(private val dataSource: DipDataSource) {

    operator fun invoke(): Flow<DipCountriesResponse?> = flow {
        dataSource.supportedCountries().collect {
            emit(it)
        }
    }
}