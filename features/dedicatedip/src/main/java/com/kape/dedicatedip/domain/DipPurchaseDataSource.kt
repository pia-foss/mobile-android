package com.kape.dedicatedip.domain

import com.kape.dedicatedip.utils.DipApiResult
import com.privateinternetaccess.account.model.response.AndroidAddonsSubscriptionsInformation
import com.privateinternetaccess.account.model.response.DipCountriesResponse

interface DipPurchaseDataSource {
    suspend fun renew(ipToken: String): DipApiResult

    suspend fun supportedCountries(): DipCountriesResponse?

    suspend fun signupPlans(): AndroidAddonsSubscriptionsInformation?

    suspend fun signup(dipPurchaseData: Any): Result<Unit>

    suspend fun fetchToken(
        countryCode: String,
        regionName: String,
    ): Result<String>
}