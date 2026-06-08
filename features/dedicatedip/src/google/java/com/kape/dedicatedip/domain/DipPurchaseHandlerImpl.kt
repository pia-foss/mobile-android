package com.kape.dedicatedip.domain

import android.app.Activity
import com.kape.data.model.DipPurchaseData
import com.kape.dedicatedip.data.models.DedicatedIpMonthlyPlan
import com.kape.dedicatedip.data.models.DedicatedIpYearlyPlan
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.payments.ui.DipSubscriptionPaymentProvider
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.coroutines.flow.Flow

class DipPurchaseHandlerImpl(
    private val getDipSupportedCountries: GetDipSupportedCountries,
    private val getDipMonthlyPlan: GetDipMonthlyPlan,
    private val getDipYearlyPlan: GetDipYearlyPlan,
    private val validateDipSignup: ValidateDipSignup,
    private val fetchSignupDipToken: FetchSignupDipToken,
    private val renewDipUseCase: RenewDipUseCase,
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
    private val dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
) : DipPurchaseHandler {
    override suspend fun getDipSupportedCountries(): DipCountriesResponse? = getDipSupportedCountries.invoke()

    override suspend fun getDipMonthlyPlan(): DedicatedIpMonthlyPlan? = getDipMonthlyPlan.invoke()

    override suspend fun getDipYearlyPlan(): DedicatedIpYearlyPlan? = getDipYearlyPlan.invoke()

    override suspend fun validateDip(dipPurchaseData: DipPurchaseData?): Result<Unit> = validateDipSignup.invoke(dipPurchaseData)

    override suspend fun renew(ipToken: String): DipApiResult = renewDipUseCase.renew(ipToken)

    override fun hasActiveSubscription(): Flow<Boolean> = vpnSubscriptionPaymentProvider.hasActiveSubscription()

    override fun purchaseProduct(
        activity: Activity,
        productId: String,
        callback: (result: Result<DipPurchaseData>) -> Unit,
    ) {
        dipSubscriptionPaymentProvider.purchaseProduct(activity, productId, callback)
    }

    override fun unacknowledgedProductIds(
        productIds: List<String>,
        callback: (result: Result<List<String>>) -> Unit,
    ) {
        dipSubscriptionPaymentProvider.unacknowledgedProductIds(productIds, callback)
    }

    override suspend fun fetchDipToken(
        countryCode: String,
        regionName: String,
    ): Result<String> = fetchSignupDipToken(countryCode, regionName)
}