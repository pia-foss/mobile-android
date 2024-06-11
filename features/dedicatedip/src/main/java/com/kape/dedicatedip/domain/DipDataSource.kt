package com.kape.dedicatedip.domain

import com.kape.dedicatedip.utils.DipApiResult
import com.privateinternetaccess.account.model.response.AndroidAddonsSubscriptionsInformation
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.coroutines.flow.Flow

interface DipDataSource {

    fun activate(ipToken: String): Flow<DipApiResult>

    fun renew(ipToken: String): Flow<DipApiResult>

    fun supportedCountries(): Flow<DipCountriesResponse?>

    fun signupPlans(): Flow<AndroidAddonsSubscriptionsInformation?>

    // Parameters to be replaced by data class with the information needed by the coming API.
    // We are mocking for now.
    fun signup(receipt: String = ""): Flow<Result<String>>
}