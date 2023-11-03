package com.kape.automation.ui

import androidx.lifecycle.ViewModel
import com.kape.automation.utils.AutomationStep
import com.kape.router.Back
import com.kape.router.ExitFlow
import com.kape.router.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent

class AutomationViewModel(private val router: Router) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow<AutomationStep>(AutomationStep.LocationPermission)
    val state: StateFlow<AutomationStep> = _state

    fun exitAutomation() = router.handleFlow(ExitFlow.Automation)
}