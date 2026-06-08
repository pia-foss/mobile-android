package com.kape.dedicatedip.domain

import com.kape.data.model.DipPurchaseData
import com.kape.payments.prefs.SubscriptionPrefs
import org.koin.core.annotation.Singleton

@Singleton
class ValidateDipSignup(
    private val subscriptionPrefs: SubscriptionPrefs,
    private val dataSource: DipPurchaseDataSource,
) {
    suspend operator fun invoke(dipPurchaseData: DipPurchaseData?): Result<Unit> {
        val unwrappedDipPurchaseData =
            dipPurchaseData?.let {
                it
            } ?: run {
                subscriptionPrefs.dipPurchaseData.value
            }

        if (unwrappedDipPurchaseData == null) {
            return Result.failure(IllegalStateException("Unknown purchase data"))
        }

        subscriptionPrefs.storeDipPurchaseData(unwrappedDipPurchaseData)
        val result = dataSource.signup(dipPurchaseData = unwrappedDipPurchaseData)
        result.fold(
            onSuccess = {
                subscriptionPrefs.removeDipPurchaseData()
            },
            onFailure = {},
        )
        return result
    }
}