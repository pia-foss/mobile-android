package com.kape.settings.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.localprefs.api.SettingsStore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class GeneralSettingsViewModel(
    private val prefs: SettingsStore,
) : ViewModel() {
    val launchOnBootEnabled: StateFlow<Boolean> = prefs.isLaunchOnStartupEnabled
    val connectOnStart: StateFlow<Boolean> = prefs.isConnectOnLaunchEnabled
    val connectOnUpdate: StateFlow<Boolean> = prefs.isConnectOnAppUpdateEnabled
    val showGeoLocatedServers: StateFlow<Boolean> = prefs.isShowGeoLocatedServersEnabled

    fun toggleLaunchOnBoot(enabled: Boolean) = save { prefs.setEnableLaunchOnStartup(enabled) }

    fun toggleConnectOnStart(enabled: Boolean) = save { prefs.setEnableConnectOnLaunch(enabled) }

    fun toggleConnectOnUpdate(enabled: Boolean) = save { prefs.setEnableConnectOnAppUpdate(enabled) }

    fun toggleShowGeoLocatedServers(enabled: Boolean) = save { prefs.setEnabledShowGeoLocatedServers(enabled) }

    private fun save(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}