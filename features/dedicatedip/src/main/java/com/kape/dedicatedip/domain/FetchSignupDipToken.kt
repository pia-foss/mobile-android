package com.kape.dedicatedip.domain

import org.koin.core.annotation.Singleton

@Singleton
class FetchSignupDipToken(
    private val dipDataSource: DipDataSource,
) {
    suspend operator fun invoke(
        countryCode: String,
        regionName: String,
    ): Result<String> = dipDataSource.fetchToken(countryCode = countryCode, regionName = regionName)
}