package com.kape.tvwelcome.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.router.EnterFlow
import com.kape.router.Exit
import com.kape.router.Router
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class TvWelcomeViewModel(
    private val router: Router,
    buildConfigProvider: BuildConfigProvider,
) : ViewModel(), KoinComponent {

    val shouldShowSubscribeButton = buildConfigProvider.isGoogleFlavor()

    fun login() = viewModelScope.launch {
        router.handleFlow(EnterFlow.TvLoginUsername)
    }

    fun signup() = viewModelScope.launch {
        router.handleFlow(EnterFlow.Subscribe)
    }

    fun exitApp() {
        router.handleFlow(Exit)
    }
}