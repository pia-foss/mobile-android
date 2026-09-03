package com.kape.dedicatedip.domain

import com.kape.dedicatedip.data.models.DedicatedIpMonthlyPlan
import com.kape.payments.utils.MONTHLY_SUBSCRIPTION
import org.koin.core.annotation.Singleton

@Singleton
class GetDipMonthlyPlan(
    private val getDipProductDetails: GetDipProductDetails,
) {
    suspend operator fun invoke(): DedicatedIpMonthlyPlan? {
        val (id, price) = getDipProductDetails(MONTHLY_SUBSCRIPTION) ?: return null
        return DedicatedIpMonthlyPlan(id = id, monthlyPrice = price)
    }
}