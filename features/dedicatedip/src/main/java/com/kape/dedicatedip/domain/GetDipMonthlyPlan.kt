package com.kape.dedicatedip.domain

import com.kape.dedicatedip.data.DipSignupRepository
import com.kape.dedicatedip.data.models.DedicatedIpMonthlyPlan
import com.kape.payments.ui.DipSubscriptionPaymentProvider
import com.kape.payments.utils.monthlySubscription
import com.kape.ui.utils.PriceFormatter
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton
class GetDipMonthlyPlan(
    private val dipSignupRepository: DipSignupRepository,
    private val dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
    private val formatter: PriceFormatter,
) {

    suspend operator fun invoke(): DedicatedIpMonthlyPlan? {
        val subscriptions = dipSignupRepository.signupPlans() ?: return null

        val monthlySubscription = subscriptions.availableProducts.firstOrNull { product ->
            product.plan.lowercase() == monthlySubscription.lowercase()
        } ?: return null

        return suspendCancellableCoroutine { cont ->
            dipSubscriptionPaymentProvider.productsDetails(
                productIds = listOf(monthlySubscription.id),
            ) { result ->
                result.fold(
                    onSuccess = { pairs ->
                        val productDetails = pairs.first()
                        cont.resume(
                            DedicatedIpMonthlyPlan(
                                id = productDetails.first,
                                monthlyPrice = formatter.formatYearlyPlan(productDetails.second),
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