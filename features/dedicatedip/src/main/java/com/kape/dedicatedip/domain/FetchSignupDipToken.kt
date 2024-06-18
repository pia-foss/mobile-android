package com.kape.dedicatedip.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FetchSignupDipToken(
    private val dipDataSource: DipDataSource,
) {

    operator fun invoke(countryCode: String, regionName: String): Flow<Result<String>> = flow {
        dipDataSource.fetchToken(countryCode = countryCode, regionName = regionName).collect {
            emit(it)
        }
    }
}