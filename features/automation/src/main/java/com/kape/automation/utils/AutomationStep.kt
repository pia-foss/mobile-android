package com.kape.automation.utils

sealed class AutomationStep {
    data object LocationPermission: AutomationStep()
    data object EnableBackgroundLocation: AutomationStep()
    data object Main: AutomationStep()
}