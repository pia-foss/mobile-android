package com.kape.dedicatedip.domain

import org.koin.core.annotation.Singleton

@Singleton
class FetchSignupDipToken(
    private val dipDataSource: DipPurchaseDataSource,
) {
    suspend operator fun invoke(
        countryCode: String,
        regionName: String,
    ): Result<String> = dipDataSource.fetchToken(countryCode = countryCode, regionName = regionName)
}