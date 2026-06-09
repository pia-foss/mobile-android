package com.kape.dedicatedip.domain

import android.content.Context
import com.kape.data.model.DipPurchaseData
import com.kape.dedicatedip.utils.DipApiResult
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.request.AndroidAddonSignupInformation
import com.privateinternetaccess.account.model.response.AndroidAddonsSubscriptionsInformation
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class DipPurchaseDataSourceImpl(
    private val context: Context,
    private val accountApi: AndroidAccountAPI,
) : DipPurchaseDataSource {
    companion object {
        private const val STORE = "google_play"
    }

    override suspend fun renew(ipToken: String): DipApiResult =
        suspendCancellableCoroutine { cont ->
            accountApi.renewDedicatedIP(ipToken) {
                if (it.isNotEmpty()) {
                    cont.resume(DipApiResult.Error)
                    return@renewDedicatedIP
                }
                cont.resume(DipApiResult.Active)
            }
        }

    override suspend fun supportedCountries(): DipCountriesResponse? =
        suspendCancellableCoroutine { cont ->
            accountApi.supportedDedicatedIPCountries { details, errors ->
                if (errors.isNotEmpty()) {
                    cont.resume(null)
                    return@supportedDedicatedIPCountries
                }
                cont.resume(details)
            }
        }

    override suspend fun signupPlans(): AndroidAddonsSubscriptionsInformation? =
        suspendCancellableCoroutine { cont ->
            accountApi.addonsSubscriptions { details, errors ->
                if (errors.isNotEmpty()) {
                    cont.resume(null)
                    return@addonsSubscriptions
                }
                cont.resume(details)
            }
        }

    override suspend fun signup(dipPurchaseData: Any): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            if (dipPurchaseData is DipPurchaseData) {
                accountApi.addonSignUp(
                    AndroidAddonSignupInformation(
                        receipt =
                            AndroidAddonSignupInformation.Receipt(
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
        }

    override suspend fun fetchToken(
        countryCode: String,
        regionName: String,
    ): Result<String> =
        suspendCancellableCoroutine { cont ->
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