package com.kape.settings.ui

import androidx.lifecycle.ViewModel
import com.kape.router.Back
import com.kape.router.EnterFlow
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.settings.utils.SettingsPrefs
import org.koin.core.component.KoinComponent

class SettingsViewModel(private val prefs: SettingsPrefs, private val router: Router) : ViewModel(), KoinComponent {

    val launchOnBootEnabled = prefs.isLaunchOnStartupEnabled()
    val connectOnStart = prefs.isConnectOnLaunchEnabled()
    val connectOnUpdate = prefs.isConnectOnAppUpdateEnabled()

    fun navigateUp() {
        router.handleFlow(Back)
    }

    fun navigateToConnection() {
        router.handleFlow(ExitFlow.Settings)
    }

    fun navigateToGeneralSettings() {
        router.handleFlow(EnterFlow.GeneralSettings)
    }

    fun toggleLaunchOnBoot(enable: Boolean) {
        prefs.enableLaunchOnStartup(enable)
    }

    fun toggleConnectOnStart(enable: Boolean) {
        prefs.enableConnectOnLaunch(enable)
    }

    fun toggleConnectOnUpdate(enable: Boolean) {
        prefs.enableConnectOnAppUpdate(enable)
    }
}