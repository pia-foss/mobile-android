package com.kape.settings.ui.vm

import android.content.pm.ApplicationInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.Router
import com.kape.localprefs.api.SettingsStore
import com.kape.settings.domain.InstalledAppsSource
import com.kape.settings.domain.usecase.EditExcludedAppsUseCase
import com.kape.settings.domain.usecase.ReconnectPrompt
import com.kape.settings.domain.usecase.SetExternalProxyAppUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

abstract class AppListViewModel(
    private val installedApps: InstalledAppsSource,
    private val connectionInfo: ConnectionInfoProvider,
) : ViewModel() {
    private val allApps = MutableStateFlow<List<ApplicationInfo>>(emptyList())
    private val query = MutableStateFlow("")

    val appList: StateFlow<List<ApplicationInfo>> =
        combine(allApps, query) { apps, query ->
            if (query.isEmpty()) apps else apps.filter { installedApps.label(it).contains(query, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    init {
        viewModelScope.launch { allApps.value = installedApps.load() }
    }

    fun filterAppsByName(value: String) {
        query.value = value
    }

    fun isConnected(): Boolean = connectionInfo.isConnected()
}

@KoinViewModel
class PerAppSettingsViewModel(
    private val excludedApps: EditExcludedAppsUseCase,
    private val router: Router,
    val reconnectPrompt: ReconnectPrompt,
    prefs: SettingsStore,
    installedApps: InstalledAppsSource,
    connectionInfo: ConnectionInfoProvider,
) : AppListViewModel(installedApps, connectionInfo) {
    val vpnExcludedApps: StateFlow<List<String>> = prefs.vpnExcludedApps

    fun addToVpnExcludedApps(packageName: String) {
        viewModelScope.launch { excludedApps.add(packageName) }
    }

    fun removeFromVpnExcludedApps(packageName: String) {
        viewModelScope.launch { excludedApps.remove(packageName) }
    }

    fun navigateBack() = router.navigateBack()
}

@KoinViewModel
class ExternalProxyAppsViewModel(
    private val setExternalProxyApp: SetExternalProxyAppUseCase,
    val reconnectPrompt: ReconnectPrompt,
    prefs: SettingsStore,
    installedApps: InstalledAppsSource,
    connectionInfo: ConnectionInfoProvider,
) : AppListViewModel(installedApps, connectionInfo) {
    val externalProxyAppPackageName: StateFlow<String> = prefs.externalProxyAppPackageName
    val vpnExcludedApps: StateFlow<List<String>> = prefs.vpnExcludedApps

    fun setExternalProxyAppPackageName(packageName: String) {
        viewModelScope.launch { setExternalProxyApp(packageName) }
    }
}

private const val STOP_TIMEOUT_MS = 5_000L