package com.kape.dedicatedip.domain

import com.kape.dedicatedip.data.models.DedicatedIpSignupPlans
import com.kape.dedicatedip.data.models.SupportedCountries
import com.kape.dedicatedip.utils.DipApiResult
import kotlinx.coroutines.flow.Flow

interface DipDataSource {

    fun activate(ipToken: String): Flow<DipApiResult>

    fun renew(ipToken: String): Flow<DipApiResult>

    fun supportedCountries(): Flow<SupportedCountries>

    fun signupPlans(): Flow<DedicatedIpSignupPlans>

    // Parameters to be replaced by data class with the information needed by the coming API.
    // We are mocking for now.
    fun signup(receipt: String = ""): Flow<Result<String>>
}