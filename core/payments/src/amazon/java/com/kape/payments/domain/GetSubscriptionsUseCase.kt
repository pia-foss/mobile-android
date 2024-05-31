package com.kape.payments.domain

import com.kape.payments.data.Subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetSubscriptionsUseCase(private val source: SubscriptionDataSource) {

    suspend fun getSubscriptions(): Flow<List<Subscription>> = flow {
        source.getAvailableSubscriptions().collect {
            emit(it)
        }
    }
}