package com.kape.signup.data

import android.app.Activity
import com.kape.signup.domain.SignupBillingHandler
import com.kape.signup.utils.SignupScreenState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class NoOpSignupBillingHandler(
    private val signupScreenState: SignupScreenState,
) : SignupBillingHandler {
    private val _billingState = MutableSharedFlow<SignupScreenState>(replay = 1)
    override val billingState: SharedFlow<SignupScreenState> = _billingState

    override fun initialize(mainDispatcher: CoroutineDispatcher) {
        // no-op
    }

    override fun loadPrices(
        mainDispatcher: CoroutineDispatcher,
        activity: Activity,
    ) {
        CoroutineScope(mainDispatcher).launch {
            _billingState.emit(signupScreenState)
        }
    }

    override fun purchase(
        id: String,
        activity: Activity,
    ) {
        // no-op
    }

    override fun registerClientIfNeeded(activity: Activity) {
        // no-op
    }

    override fun reset() {
        // no-op
    }

    override suspend fun hasResumablePurchase(): Boolean = false

    override fun hasActiveSubscription(): Flow<Boolean> = flowOf(false)

    override suspend fun registerAndAwaitReady(
        mainDispatcher: CoroutineDispatcher,
        activity: Activity,
    ) {
        // no-op
    }
}