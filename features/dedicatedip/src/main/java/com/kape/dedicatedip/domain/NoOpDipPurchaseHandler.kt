package com.kape.dedicatedip.domain

import android.app.Activity
import com.kape.data.model.DipPurchaseData
import com.kape.dedicatedip.data.models.DedicatedIpMonthlyPlan
import com.kape.dedicatedip.data.models.DedicatedIpYearlyPlan
import com.kape.dedicatedip.utils.DipApiResult
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class NoOpDipPurchaseHandler : DipPurchaseHandler {
    override suspend fun getDipSupportedCountries(): DipCountriesResponse? = null

    override suspend fun getDipMonthlyPlan(): DedicatedIpMonthlyPlan? = null

    override suspend fun getDipYearlyPlan(): DedicatedIpYearlyPlan? = null

    override suspend fun validateDip(dipPurchaseData: DipPurchaseData?): Result<Unit> = Result.failure(Throwable("No-op"))

    override suspend fun renew(ipToken: String): DipApiResult = DipApiResult.Invalid

    override fun hasActiveSubscription(): Flow<Boolean> = flowOf(false)

    override fun purchaseProduct(
        activity: Activity,
        productId: String,
        callback: (result: Result<DipPurchaseData>) -> Unit,
    ) {
        // no-op
    }

    override fun unacknowledgedProductIds(
        productIds: List<String>,
        callback: (result: Result<List<String>>) -> Unit,
    ) {
        // no-op
    }

    override suspend fun fetchDipToken(
        countryCode: String,
        regionName: String,
    ): Result<String> = Result.failure(Throwable("No-op"))
}