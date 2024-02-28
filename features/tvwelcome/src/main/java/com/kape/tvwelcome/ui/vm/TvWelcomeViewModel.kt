package com.kape.tvwelcome.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.router.EnterFlow
import com.kape.router.Router
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class TvWelcomeViewModel(
    private val router: Router,
) : ViewModel(), KoinComponent {

    fun login() = viewModelScope.launch {
        router.handleFlow(EnterFlow.Login)
    }

    fun signup() = viewModelScope.launch {
        router.handleFlow(EnterFlow.Subscribe)
    }
}