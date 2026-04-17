package com.kape.dedicatedip.domain

import com.kape.payments.SubscriptionPrefs
import com.kape.payments.data.DipPurchaseData
import org.koin.core.annotation.Singleton

@Singleton
class ValidateDipSignup(
    private val subscriptionPrefs: SubscriptionPrefs,
    private val dataSource: DipDataSource,
) {

    suspend operator fun invoke(
        dipPurchaseData: DipPurchaseData?,
    ): Result<Unit> {
        val unwrappedDipPurchaseData = dipPurchaseData?.let {
            it
        } ?: run {
            subscriptionPrefs.getDipPurchaseData()
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