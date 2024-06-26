package com.kape.settings.utils

sealed class SettingsStep {
    data object Main : SettingsStep()
    data object General : SettingsStep()
    data object Protocol : SettingsStep()
    data object Network : SettingsStep()
    data object Privacy : SettingsStep()
    data object Help : SettingsStep()
    data object Automation : SettingsStep()
    data object Obfuscation : SettingsStep()
    data object KillSwitch : SettingsStep()
    data object ConnectionStats : SettingsStep()
    data object DebugLogs : SettingsStep()
    data object ShortcutProtocol : SettingsStep()
    data object ShortcutAutomation : SettingsStep()
    data object ShortcutKillSwitch : SettingsStep()
    data object ExternalProxyAppList : SettingsStep()
}