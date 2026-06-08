package com.kape.payments.domain

import com.kape.data.model.Subscription

interface SubscriptionDataSource {
    suspend fun getAvailableVpnSubscriptions(): List<Subscription>
}