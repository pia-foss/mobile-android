package com.kape.dedicatedip.di

import android.app.Activity
import com.kape.data.model.DipPurchaseData
import com.kape.dedicatedip.domain.DipCountry
import com.kape.dedicatedip.domain.DipMonthlyPlan
import com.kape.dedicatedip.domain.DipPurchaseStore
import com.kape.dedicatedip.domain.DipYearlyPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class DipStoreModule {
    @Singleton(binds = [DipPurchaseStore::class])
    fun provideStore(): NoDipPurchaseStore = NoDipPurchaseStore()
}

class NoDipPurchaseStore : DipPurchaseStore {
    private val notSold = Result.failure<Nothing>(UnsupportedOperationException("Dedicated IPs aren't sold in this store"))

    override suspend fun supportedCountries(): List<DipCountry>? = null

    override suspend fun monthlyPlan(): DipMonthlyPlan? = null

    override suspend fun yearlyPlan(): DipYearlyPlan? = null

    override suspend fun purchase(
        activity: Activity,
        productId: String,
    ): Result<DipPurchaseData> = notSold

    override suspend fun unregisteredPurchases(productIds: List<String>): List<String>? = null

    override suspend fun registerPurchase(purchase: DipPurchaseData?): Result<Unit> = notSold

    override suspend fun fetchToken(
        countryCode: String,
        regionName: String,
    ): Result<String> = notSold

    override fun hasActiveVpnSubscription(): Flow<Boolean> = flowOf(false)

    override suspend fun renew(dipToken: String): Boolean = false
}