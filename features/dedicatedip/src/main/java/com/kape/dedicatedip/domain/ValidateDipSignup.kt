package com.kape.dedicatedip.domain

import com.kape.payments.SubscriptionPrefs
import com.kape.payments.data.DipPurchaseData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ValidateDipSignup(
    private val subscriptionPrefs: SubscriptionPrefs,
    private val dataSource: DipDataSource,
) {

    operator fun invoke(dipPurchaseData: DipPurchaseData): Flow<Result<Unit>> = flow {
        subscriptionPrefs.storeDipPurchaseData(dipPurchaseData)
        dataSource.signup(dipPurchaseData = dipPurchaseData).collect { result ->
            result.fold(
                onSuccess = {
                    subscriptionPrefs.removeDipPurchaseData()
                    emit(result)
                },
                onFailure = {
                    emit(result)
                },
            )
        }
    }
}