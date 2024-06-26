package com.kape.payments.domain

import com.kape.payments.data.Subscription
import kotlinx.coroutines.flow.Flow

interface SubscriptionDataSource {

    fun getAvailableVpnSubscriptions(): Flow<List<Subscription>>
}