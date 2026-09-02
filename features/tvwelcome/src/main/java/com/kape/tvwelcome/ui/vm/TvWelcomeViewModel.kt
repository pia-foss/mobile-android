package com.kape.tvwelcome.ui.vm

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.data.TvLoginUsername
import com.kape.data.TvSubscribe
import com.kape.signup.domain.SignupBillingHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class TvWelcomeViewModel(
    private val router: Router,
    private val billingHandler: SignupBillingHandler,
    buildConfigProvider: BuildConfigProvider,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
    @Named(DI.MAIN_DISPATCHER) private val mainDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val isGoogleFlavor = buildConfigProvider.isGoogleFlavor()

    private val _shouldShowSubscribeButton = MutableStateFlow(isGoogleFlavor && billingHandler.isBillingAvailable())
    val shouldShowSubscribeButton: StateFlow<Boolean> = _shouldShowSubscribeButton

    fun checkBillingAvailability(activity: Activity) {
        if (!isGoogleFlavor) return
        viewModelScope.launch(ioDispatcher) {
            billingHandler.registerAndAwaitReady(mainDispatcher, activity)
            _shouldShowSubscribeButton.value = billingHandler.isBillingAvailable()
        }
    }

    fun login() = router.updateDestination(TvLoginUsername)

    fun signup() = router.updateDestination(TvSubscribe)
}