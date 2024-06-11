package com.kape.dedicatedip.domain

import com.kape.dedicatedip.data.DipSignupRepository
import com.kape.dedicatedip.data.models.DedicatedIpMonthlyPlan
import com.kape.payments.ui.DipSubscriptionPaymentProvider
import com.kape.payments.utils.monthlySubscription
import com.kape.ui.utils.PriceFormatter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GetDipMonthlyPlan(
    private val dipSignupRepository: DipSignupRepository,
    private val dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
    private val formatter: PriceFormatter,
) {

    operator fun invoke(): Flow<DedicatedIpMonthlyPlan?> = callbackFlow {
        dipSignupRepository.signupPlans().collect { subscriptions ->
            if (subscriptions == null) {
                trySend(null)
                return@collect
            }

            val monthlySubscription = subscriptions.availableProducts.firstOrNull { product ->
                product.plan.lowercase() == monthlySubscription.lowercase()
            }

            if (monthlySubscription == null) {
                trySend(null)
                return@collect
            }

            dipSubscriptionPaymentProvider.productsDetails(
                productIds = listOf(monthlySubscription.id),
            ) { result ->
                result.fold(
                    onSuccess = { pairs ->
                        val productDetails = pairs.first()
                        trySend(
                            DedicatedIpMonthlyPlan(
                                id = productDetails.first,
                                monthlyPrice = formatter.formatYearlyPlan(productDetails.second),
                            ),
                        )
                    },
                    onFailure = {
                        trySend(null)
                    },
                )
            }
        }
        awaitClose { channel.close() }
    }
}