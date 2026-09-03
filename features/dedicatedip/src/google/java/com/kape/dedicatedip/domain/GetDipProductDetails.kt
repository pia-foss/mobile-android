package com.kape.dedicatedip.domain

import com.kape.dedicatedip.data.DipSignupRepository
import com.kape.payments.ui.DipSubscriptionPaymentProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton
class GetDipProductDetails(
    private val dipSignupRepository: DipSignupRepository,
    private val dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
) {
    suspend operator fun invoke(planType: String): Pair<String, String>? {
        val subscriptions = dipSignupRepository.signupPlans() ?: return null

        val product =
            subscriptions.availableProducts.firstOrNull { it.plan.lowercase() == planType.lowercase() }
                ?: return null

        return suspendCancellableCoroutine { cont ->
            dipSubscriptionPaymentProvider.productsDetails(
                productIds = listOf(product.id),
            ) { result ->
                result.fold(
                    onSuccess = { pairs -> cont.resume(pairs.first()) },
                    onFailure = { cont.resume(null) },
                )
            }
        }
    }
}