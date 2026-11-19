package com.kape.dedicatedip.domain

import android.app.Activity
import com.kape.data.model.DipPurchaseData
import com.kape.dip.data.DedicatedIpSelectedCountry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface DedicatedIpRepository {
    suspend fun activate(token: String): DipActivationResult

    suspend fun remove(dipToken: String): Boolean
}

interface ServerSelectionRepository {
    suspend fun selectedServerKey(): String?

    suspend fun clearSelectedServer()

    suspend fun removeFromQuickConnect(serverKey: String)
}

interface DipSignupStateRepository {
    val selectedCountry: StateFlow<DedicatedIpSelectedCountry?>

    /** The stored selection, read directly; [selectedCountry] only updates while collected. */
    suspend fun selectedCountryNow(): DedicatedIpSelectedCountry?

    suspend fun selectCountry(country: DedicatedIpSelectedCountry)

    val selectedProductId: StateFlow<String>

    suspend fun selectProduct(productId: String)

    val purchasedToken: StateFlow<String>

    suspend fun setPurchasedToken(token: String)

    suspend fun clearPurchasedToken()

    fun isSignupEnabled(isGooglePlayBuild: Boolean): Flow<Boolean>
}

interface DipPurchaseStore {
    suspend fun supportedCountries(): List<DipCountry>?

    suspend fun monthlyPlan(): DipMonthlyPlan?

    suspend fun yearlyPlan(): DipYearlyPlan?

    suspend fun purchase(
        activity: Activity,
        productId: String,
    ): Result<DipPurchaseData>

    suspend fun unregisteredPurchases(productIds: List<String>): List<String>?

    suspend fun registerPurchase(purchase: DipPurchaseData?): Result<Unit>

    suspend fun fetchToken(
        countryCode: String,
        regionName: String,
    ): Result<String>

    fun hasActiveVpnSubscription(): Flow<Boolean>

    suspend fun renew(dipToken: String): Boolean
}