package com.kape.contracts

import android.app.Activity
import kotlinx.coroutines.flow.Flow

interface SubscriptionBilling {
    fun hasActiveSubscription(): Flow<Boolean>

    suspend fun initialize()

    suspend fun awaitReady(activity: Activity)

    fun loadPrices(activity: Activity)

    fun isAvailable(): Boolean
}