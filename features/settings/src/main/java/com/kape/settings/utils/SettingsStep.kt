package com.kape.settings.utils

sealed class SettingsStep {
    data object Main : SettingsStep()
    data object General : SettingsStep()
    data class Protocol(val isShortcut: Boolean = false) : SettingsStep()
    data object Network : SettingsStep()
    data object Privacy : SettingsStep()
    data object Help : SettingsStep()
    data class Automation(val isShortcut: Boolean = false) : SettingsStep()
    data class KillSwitch(val isShortcut: Boolean = false) : SettingsStep()
    data object ConnectionStats : SettingsStep()
    data object DebugLogs : SettingsStep()
}