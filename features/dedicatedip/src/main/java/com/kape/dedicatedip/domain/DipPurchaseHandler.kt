package com.kape.dedicatedip.domain

import android.app.Activity
import com.kape.data.dip.DipPurchaseData
import com.kape.dedicatedip.data.models.DedicatedIpMonthlyPlan
import com.kape.dedicatedip.data.models.DedicatedIpYearlyPlan
import com.kape.dedicatedip.utils.DipApiResult
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.coroutines.flow.Flow

interface DipPurchaseHandler {
    suspend fun getDipSupportedCountries(): DipCountriesResponse?

    suspend fun getDipMonthlyPlan(): DedicatedIpMonthlyPlan?

    suspend fun getDipYearlyPlan(): DedicatedIpYearlyPlan?

    suspend fun validateDip(dipPurchaseData: DipPurchaseData?): Result<Unit>

    suspend fun renew(ipToken: String): DipApiResult

    fun hasActiveSubscription(): Flow<Boolean>

    fun purchaseProduct(
        activity: Activity,
        productId: String,
        callback: (result: Result<DipPurchaseData>) -> Unit,
    )

    fun unacknowledgedProductIds(
        productIds: List<String>,
        callback: (result: Result<List<String>>) -> Unit,
    )

    suspend fun fetchDipToken(
        countryCode: String,
        regionName: String,
    ): Result<String>
}