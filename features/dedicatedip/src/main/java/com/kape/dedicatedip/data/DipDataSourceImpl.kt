package com.kape.dedicatedip.data

import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.dip.DipPrefs
import com.kape.dip.data.DIP_SIGNUP_MOCKED_RESPONSE
import com.kape.dip.data.FetchedDedicatedIpSignupPlansMock
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json

class DipDataSourceImpl(
    private val accountApi: AndroidAccountAPI,
    private val dipPrefs: DipPrefs,
) : DipDataSource {

    override fun activate(ipToken: String): Flow<DipApiResult> = callbackFlow {
        accountApi.redeemDedicatedIPs(listOf(ipToken)) { details, errors ->
            if (errors.isNotEmpty()) {
                trySend(DipApiResult.Error)
                return@redeemDedicatedIPs
            }
            val activated = details.firstOrNull { it.dipToken == ipToken }
            activated?.let {
                when (it.status) {
                    DedicatedIPInformationResponse.Status.active -> {
                        dipPrefs.addDedicatedIp(it)
                        trySend(DipApiResult.Active)
                    }

                    DedicatedIPInformationResponse.Status.expired -> {
                        trySend(DipApiResult.Expired)
                    }

                    DedicatedIPInformationResponse.Status.invalid -> {
                        trySend(DipApiResult.Invalid)
                    }

                    DedicatedIPInformationResponse.Status.error -> {
                        trySend(DipApiResult.Error)
                    }
                }
            } ?: run {
                trySend(DipApiResult.Error)
            }
        }
        awaitClose { channel.close() }
    }

    override fun renew(ipToken: String): Flow<DipApiResult> = callbackFlow {
        accountApi.renewDedicatedIP(ipToken) {
            if (it.isNotEmpty()) {
                trySend(DipApiResult.Error)
                return@renewDedicatedIP
            }
            // todo: update if needed once implemented
            trySend(DipApiResult.Active)
        }
        awaitClose { channel.close() }
    }

    override fun supportedCountries(): Flow<DipCountriesResponse?> = callbackFlow {
        accountApi.supportedDedicatedIPCountries { details, errors ->
            if (errors.isNotEmpty()) {
                trySend(null)
                return@supportedDedicatedIPCountries
            }
            trySend(details)
        }
        awaitClose { channel.close() }
    }

    override fun signupPlans(): Flow<FetchedDedicatedIpSignupPlansMock> = callbackFlow {
        trySend(Json.decodeFromString(DIP_SIGNUP_MOCKED_RESPONSE))
        awaitClose { channel.close() }
    }

    override fun signup(receipt: String): Flow<Result<String>> = callbackFlow {
        trySend(Result.success("DIP_123456789"))
        awaitClose { channel.close() }
    }
}