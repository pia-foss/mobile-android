package com.kape.dedicatedip.domain

import com.kape.dedicatedip.utils.DipApiResult
import com.privateinternetaccess.account.model.response.AndroidAddonsSubscriptionsInformation
import com.privateinternetaccess.account.model.response.DipCountriesResponse

class NoOpDipPurchaseDataSource : DipPurchaseDataSource {
    override suspend fun renew(ipToken: String): DipApiResult = DipApiResult.Invalid

    override suspend fun supportedCountries(): DipCountriesResponse? = null

    override suspend fun signupPlans(): AndroidAddonsSubscriptionsInformation? = null

    override suspend fun signup(dipPurchaseData: Any): Result<Unit> = Result.failure(Throwable("not supported"))

    override suspend fun fetchToken(
        countryCode: String,
        regionName: String,
    ): Result<String> = Result.failure(Throwable("not supported"))
}