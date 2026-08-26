package com.kape.signup.domain

import android.app.Activity
import com.kape.signup.utils.SignupScreenState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface SignupBillingHandler {
    val billingState: SharedFlow<SignupScreenState>

    fun initialize(mainDispatcher: CoroutineDispatcher)

    fun loadPrices(
        mainDispatcher: CoroutineDispatcher,
        activity: Activity,
    )

    fun purchase(
        id: String,
        activity: Activity,
    )

    fun registerClientIfNeeded(activity: Activity)

    fun reset()

    suspend fun hasResumablePurchase(): Boolean

    fun hasActiveSubscription(): Flow<Boolean>

    suspend fun registerAndAwaitReady(
        mainDispatcher: CoroutineDispatcher,
        activity: Activity,
    )
}