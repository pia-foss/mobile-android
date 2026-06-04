package com.kape.signup.domain

import android.app.Activity
import com.kape.signup.utils.SignupScreenState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface SignupBillingHandler {
    val billingState: Flow<SignupScreenState>

    fun initialize(
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
    )

    fun loadPrices(
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
    )

    fun purchase(id: String)

    fun registerClientIfNeeded(activity: Activity)

    fun reset()
}