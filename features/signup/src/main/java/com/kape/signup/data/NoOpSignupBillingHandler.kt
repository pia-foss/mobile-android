package com.kape.signup.data

import android.app.Activity
import com.kape.signup.domain.SignupBillingHandler
import com.kape.signup.utils.SignupScreenState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class NoOpSignupBillingHandler(
    private val loadPricesState: SignupScreenState,
) : SignupBillingHandler {
    private val _billingState = MutableSharedFlow<SignupScreenState>(replay = 1)
    override val billingState: Flow<SignupScreenState> = _billingState

    override fun initialize(
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
    ) {
        // no-op
    }

    override fun loadPrices(
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
    ) {
        scope.launch(dispatcher) {
            _billingState.emit(loadPricesState)
        }
    }

    override fun purchase(id: String) {
        // no-op
    }

    override fun registerClientIfNeeded(activity: Activity) {
        // no-op
    }

    override fun reset() {
        // no-op
    }
}