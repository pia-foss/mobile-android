package com.kape.dedicatedip.domain

import com.kape.dedicatedip.utils.DipApiResult
import com.kape.payments.data.DipPurchaseData
import com.privateinternetaccess.account.model.response.AndroidAddonsSubscriptionsInformation
import com.privateinternetaccess.account.model.response.DipCountriesResponse

interface DipDataSource {

    suspend fun activate(ipToken: String): DipApiResult

    suspend fun renew(ipToken: String): DipApiResult

    suspend fun supportedCountries(): DipCountriesResponse?

    suspend fun signupPlans(): AndroidAddonsSubscriptionsInformation?

    suspend fun signup(dipPurchaseData: DipPurchaseData): Result<Unit>

    suspend fun fetchToken(countryCode: String, regionName: String): Result<String>
}