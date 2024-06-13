package com.kape.dedicatedip.data

import android.content.Context
import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.dip.DipPrefs
import com.kape.payments.data.DipPurchaseData
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.request.AndroidAddonSignupInformation
import com.privateinternetaccess.account.model.response.AndroidAddonsSubscriptionsInformation
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DipDataSourceImpl(
    private val context: Context,
    private val accountApi: AndroidAccountAPI,
    private val dipPrefs: DipPrefs,
) : DipDataSource {

    companion object {
        private const val STORE = "google_play"
    }

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

    override fun signupPlans(): Flow<AndroidAddonsSubscriptionsInformation?> = callbackFlow {
        accountApi.addonsSubscriptions { details, errors ->
            if (errors.isNotEmpty()) {
                trySend(null)
                return@addonsSubscriptions
            }
            trySend(details)
        }
        awaitClose { channel.close() }
    }

    override fun signup(dipPurchaseData: DipPurchaseData): Flow<Result<Unit>> = callbackFlow {
        accountApi.addonSignUp(
            AndroidAddonSignupInformation(
                receipt = AndroidAddonSignupInformation.Receipt(
                    applicationPackage = context.packageName,
                    productId = dipPurchaseData.productId,
                    orderId = dipPurchaseData.orderId,
                    token = dipPurchaseData.token,
                ),
                store = STORE,
            ),
        ) { errors ->
            if (errors.isNotEmpty()) {
                trySend(Result.failure(IllegalStateException("Signup validation failed")))
                return@addonSignUp
            }
            trySend(Result.success(Unit))
        }
        awaitClose { channel.close() }
    }
}