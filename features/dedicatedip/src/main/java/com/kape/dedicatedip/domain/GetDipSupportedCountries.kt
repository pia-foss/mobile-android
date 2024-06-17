package com.kape.dedicatedip.domain

import com.kape.dedicatedip.data.DipSignupRepository
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetDipSupportedCountries(
    private val dipSignupRepository: DipSignupRepository,
) {

    operator fun invoke(): Flow<DipCountriesResponse?> = flow {
        dipSignupRepository.dipSupportedCountries().collect {
            emit(it)
        }
    }
}