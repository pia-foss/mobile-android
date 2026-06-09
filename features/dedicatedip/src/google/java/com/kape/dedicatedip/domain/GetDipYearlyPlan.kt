package com.kape.dedicatedip.domain

import com.kape.dedicatedip.data.DipSignupRepository
import com.kape.dedicatedip.data.models.DedicatedIpYearlyPlan
import com.kape.payments.ui.DipSubscriptionPaymentProvider
import com.kape.payments.utils.YEARLY_SUBSCRIPTION
import com.kape.ui.utils.PriceFormatter
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton
class GetDipYearlyPlan(
    private val dipSignupRepository: DipSignupRepository,
    private val dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
    private val formatter: PriceFormatter,
) {
    suspend operator fun invoke(): DedicatedIpYearlyPlan? {
        val subscriptions = dipSignupRepository.signupPlans() ?: return null

        val yearlySubscription =
            subscriptions.availableProducts.firstOrNull { product ->
                product.plan.lowercase() == YEARLY_SUBSCRIPTION.lowercase()
            } ?: return null

        return suspendCancellableCoroutine { cont ->
            dipSubscriptionPaymentProvider.productsDetails(
                productIds = listOf(yearlySubscription.id),
            ) { result ->
                result.fold(
                    onSuccess = { pairs ->
                        val productDetails = pairs.first()
                        cont.resume(
                            DedicatedIpYearlyPlan(
                                id = productDetails.first,
                                yearlyPrice = formatter.formatYearlyPlan(productDetails.second),
                                monthlyPrice = formatter.formatYearlyPerMonth(productDetails.second, "USD"),
                            ),
                        )
                    },
                    onFailure = {
                        cont.resume(null)
                    },
                )
            }
        }
    }
}