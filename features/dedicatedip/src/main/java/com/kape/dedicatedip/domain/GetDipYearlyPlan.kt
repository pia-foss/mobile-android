package com.kape.dedicatedip.domain

import com.kape.dedicatedip.data.models.DedicatedIpYearlyPlan
import com.kape.payments.utils.yearlySubscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetDipYearlyPlan(private val dataSource: DipDataSource) {

    operator fun invoke(): Flow<DedicatedIpYearlyPlan> = flow {
        dataSource.signupPlans().collect { plans ->
            val yearlyPlan = plans.availableProducts.first {
                it.plan.lowercase() == yearlySubscription.lowercase()
            }
            emit(
                DedicatedIpYearlyPlan(
                    id = yearlyPlan.id,
                    yearlyPrice = yearlyPlan.price.toDouble(),
                    monthlyPrice = yearlyPlan.price.toDouble() / 12.0,
                ),
            )
        }
    }
}