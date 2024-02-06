package com.kape.automation.utils

sealed class AutomationStep {
    data object LocationPermission : AutomationStep()
    data object EnableBackgroundLocation : AutomationStep()
    data object MainSet : AutomationStep()
    data object MainUpdate : AutomationStep()
    data object AddRule : AutomationStep()
}