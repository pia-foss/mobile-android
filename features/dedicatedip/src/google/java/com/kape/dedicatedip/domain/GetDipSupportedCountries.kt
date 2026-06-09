package com.kape.dedicatedip.domain

import com.kape.dedicatedip.data.DipSignupRepository
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import org.koin.core.annotation.Singleton

@Singleton
class GetDipSupportedCountries(
    private val dipSignupRepository: DipSignupRepository,
) {
    suspend operator fun invoke(): DipCountriesResponse? = dipSignupRepository.dipSupportedCountries()
}