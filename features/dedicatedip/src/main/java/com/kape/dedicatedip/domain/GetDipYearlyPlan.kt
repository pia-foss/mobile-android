package com.kape.dedicatedip.domain

import com.kape.dedicatedip.data.DipSignupRepository
import com.kape.dedicatedip.data.models.DedicatedIpYearlyPlan
import com.kape.payments.ui.DipSubscriptionPaymentProvider
import com.kape.payments.utils.yearlySubscription
import com.kape.ui.utils.PriceFormatter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GetDipYearlyPlan(
    private val dipSignupRepository: DipSignupRepository,
    private val dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
    private val formatter: PriceFormatter,
) {

    operator fun invoke(): Flow<DedicatedIpYearlyPlan?> = callbackFlow {
        dipSignupRepository.signupPlans().collect { subscriptions ->
            if (subscriptions == null) {
                trySend(null)
                return@collect
            }

            val yearlySubscription = subscriptions.availableProducts.firstOrNull { product ->
                product.plan.lowercase() == yearlySubscription.lowercase()
            }

            if (yearlySubscription == null) {
                trySend(null)
                return@collect
            }

            dipSubscriptionPaymentProvider.productsDetails(
                productIds = listOf(yearlySubscription.id),
            ) { result ->
                result.fold(
                    onSuccess = { pairs ->
                        val productDetails = pairs.first()
                        trySend(
                            DedicatedIpYearlyPlan(
                                id = productDetails.first,
                                yearlyPrice = formatter.formatYearlyPlan(productDetails.second),
                                monthlyPrice = formatter.formatYearlyPerMonth(productDetails.second),
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