package com.kape.payments.domain

import com.kape.payments.data.Subscription
import org.koin.core.annotation.Singleton

@Singleton
class GetSubscriptionsUseCase(
    private val source: SubscriptionDataSource,
) {
    suspend fun getVpnSubscriptions(): List<Subscription> = emptyList()
}