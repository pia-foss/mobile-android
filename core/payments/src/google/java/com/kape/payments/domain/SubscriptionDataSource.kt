package com.kape.payments.domain

import com.kape.payments.data.Subscription

interface SubscriptionDataSource {
    suspend fun getAvailableVpnSubscriptions(): List<Subscription>
}