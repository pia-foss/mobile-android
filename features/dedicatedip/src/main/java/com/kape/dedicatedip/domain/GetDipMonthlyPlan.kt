package com.kape.dedicatedip.domain

import com.kape.dedicatedip.data.models.DedicatedIpMonthlyPlan
import com.kape.payments.utils.monthlySubscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetDipMonthlyPlan(private val dataSource: DipDataSource) {

    operator fun invoke(): Flow<DedicatedIpMonthlyPlan> = flow {
        dataSource.signupPlans().collect { plans ->
            val monthlyPlan = plans.availableProducts.first {
                it.plan.lowercase() == monthlySubscription.lowercase()
            }
            emit(
                DedicatedIpMonthlyPlan(
                    id = monthlyPlan.id,
                    monthlyPrice = monthlyPlan.price.toDouble(),
                ),
            )
        }
    }
}