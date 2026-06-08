package com.kape.payments.domain

import com.kape.data.dip.Subscription

interface SubscriptionDataSource {
    suspend fun getAvailableVpnSubscriptions(): List<Subscription>
}