package com.kape.signup.domain

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SubscriptionStore {
    val offer: StateFlow<StoreOffer>

    val purchaseCompleted: Flow<Unit>

    fun hasActiveSubscription(): Flow<Boolean>

    suspend fun hasResumablePurchase(): Boolean

    fun register(activity: Activity)

    fun loadPlans(activity: Activity)

    fun purchase(
        planId: String,
        activity: Activity,
    )

    fun reset()
}

fun interface PurchaseSignupRepository {
    suspend fun signUp(email: String): Credentials?
}

interface SharingConsentRepository {
    suspend fun setAllowSharing(allow: Boolean)

    suspend fun hasMadeDecision(): Boolean
}