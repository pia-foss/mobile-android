package com.kape.dedicatedip.data

import android.content.Context
import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.localprefs.prefs.DipPrefs
import com.kape.payments.data.DipPurchaseData
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.request.AndroidAddonSignupInformation
import com.privateinternetaccess.account.model.response.AndroidAddonsSubscriptionsInformation
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton([DipDataSource::class])
class DipDataSourceImpl(
    private val context: Context,
    private val accountApi: AndroidAccountAPI,
    private val dipPrefs: DipPrefs,
) : DipDataSource {

    companion object {
        private const val STORE = "google_play"
    }

    override suspend fun activate(ipToken: String): DipApiResult = suspendCancellableCoroutine { cont ->
        accountApi.redeemDedicatedIPs(listOf(ipToken)) { details, errors ->
            if (errors.isNotEmpty()) {
                cont.resume(DipApiResult.Error)
                return@redeemDedicatedIPs
            }
            val activated = details.firstOrNull { it.dipToken == ipToken }
            activated?.let {
                when (it.status) {
                    DedicatedIPInformationResponse.Status.active -> {
                        dipPrefs.addDedicatedIp(it)
                        cont.resume(DipApiResult.Active)
                    }

                    DedicatedIPInformationResponse.Status.expired -> {
                        cont.resume(DipApiResult.Expired)
                    }

                    DedicatedIPInformationResponse.Status.invalid -> {
                        cont.resume(DipApiResult.Invalid)
                    }

                    DedicatedIPInformationResponse.Status.error -> {
                        cont.resume(DipApiResult.Error)
                    }
                }
            } ?: run {
                cont.resume(DipApiResult.Error)
            }
        }
    }

    override suspend fun renew(ipToken: String): DipApiResult = suspendCancellableCoroutine { cont ->
        accountApi.renewDedicatedIP(ipToken) {
            if (it.isNotEmpty()) {
                cont.resume(DipApiResult.Error)
                return@renewDedicatedIP
            }
            // todo: update if needed once implemented
            cont.resume(DipApiResult.Active)
        }
    }

    override suspend fun supportedCountries(): DipCountriesResponse? = suspendCancellableCoroutine { cont ->
        accountApi.supportedDedicatedIPCountries { details, errors ->
            if (errors.isNotEmpty()) {
                cont.resume(null)
                return@supportedDedicatedIPCountries
            }
            cont.resume(details)
        }
    }

    override suspend fun signupPlans(): AndroidAddonsSubscriptionsInformation? = suspendCancellableCoroutine { cont ->
        accountApi.addonsSubscriptions { details, errors ->
            if (errors.isNotEmpty()) {
                cont.resume(null)
                return@addonsSubscriptions
            }
            cont.resume(details)
        }
    }

    override suspend fun signup(dipPurchaseData: DipPurchaseData): Result<Unit> = suspendCancellableCoroutine { cont ->
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
                cont.resume(Result.failure(IllegalStateException("Signup validation failed")))
                return@addonSignUp
            }
            cont.resume(Result.success(Unit))
        }
    }

    override suspend fun fetchToken(
        countryCode: String,
        regionName: String,
    ): Result<String> = suspendCancellableCoroutine { cont ->
        accountApi.getDedicatedIP(
            countryCode = countryCode,
            regionName = regionName,
        ) { details, errors ->
            if (details == null || errors.isNotEmpty()) {
                cont.resume(Result.failure(IllegalStateException("Fetch token failed")))
                return@getDedicatedIP
            }
            cont.resume(Result.success(details.token))
        }
    }
}