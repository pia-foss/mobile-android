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
        mainDispatcher: CoroutineDispatcher,
    )

    fun loadPrices(
        scope: CoroutineScope,
        ioDispatcher: CoroutineDispatcher,
        mainDispatcher: CoroutineDispatcher,
        activity: Activity,
    )

    fun purchase(
        id: String,
        activity: Activity,
    )

    fun registerClientIfNeeded(activity: Activity)

    fun reset()

    // True when a purchase already completed (its token is durably persisted) but the
    // account for it was never created - e.g. the app was killed on the email-entry step
    // before submitting. Lets the caller resume straight at email entry instead of sending
    // the user back through plan selection to buy a subscription they already paid for.
    suspend fun hasResumablePurchase(): Boolean
}