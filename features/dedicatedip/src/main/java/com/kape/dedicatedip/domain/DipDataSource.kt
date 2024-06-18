package com.kape.dedicatedip.domain

import com.kape.dedicatedip.utils.DipApiResult
import com.kape.payments.data.DipPurchaseData
import com.privateinternetaccess.account.model.response.AndroidAddonsSubscriptionsInformation
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.coroutines.flow.Flow

interface DipDataSource {

    fun activate(ipToken: String): Flow<DipApiResult>

    fun renew(ipToken: String): Flow<DipApiResult>

    fun supportedCountries(): Flow<DipCountriesResponse?>

    fun signupPlans(): Flow<AndroidAddonsSubscriptionsInformation?>

    fun signup(dipPurchaseData: DipPurchaseData): Flow<Result<Unit>>

    fun fetchToken(countryCode: String, regionName: String): Flow<Result<String>>
}