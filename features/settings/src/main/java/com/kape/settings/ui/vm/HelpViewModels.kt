package com.kape.settings.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.AppInfo
import com.kape.contracts.AppUpdateStatus
import com.kape.contracts.DebugLogCleaner
import com.kape.contracts.Router
import com.kape.contracts.UsageEventsControl
import com.kape.data.About
import com.kape.data.ConnectionStats
import com.kape.data.DebugLogs
import com.kape.data.WebDestination
import com.kape.localprefs.api.ConsentStore
import com.kape.localprefs.api.SettingsStore
import com.kape.settings.domain.usecase.SendDebugReportUseCase
import com.kape.settings.domain.usecase.SetDebugLoggingUseCase
import com.kape.vpnconnect.domain.GetLogsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class HelpViewModel(
    private val router: Router,
    private val consentPrefs: ConsentStore,
    private val setDebugLogging: SetDebugLoggingUseCase,
    private val sendDebugReport: SendDebugReportUseCase,
    private val appUpdateStatus: AppUpdateStatus,
    private val appInfo: AppInfo,
    prefs: SettingsStore,
) : ViewModel() {
    val debugLoggingEnabled: StateFlow<Boolean> = prefs.isDebugLoggingEnabled
    val improvePiaEnabled: StateFlow<Boolean> = consentPrefs.allowSharing
    val hasUpdateAvailable: StateFlow<Boolean> = appUpdateStatus.hasUpdateAvailable

    private val _requestId = MutableStateFlow<String?>(null)

    val requestId: StateFlow<String?> = _requestId.asStateFlow()

    fun toggleDebugLogging(enabled: Boolean) {
        viewModelScope.launch { setDebugLogging(enabled) }
    }

    fun toggleImprovePia(enabled: Boolean) {
        viewModelScope.launch { consentPrefs.setAllowSharing(enabled) }
    }

    fun sendLogs() {
        viewModelScope.launch { _requestId.value = sendDebugReport().orEmpty() }
    }

    fun resetRequestId() {
        _requestId.value = null
    }

    suspend fun getDownloadUrl(): String? = appUpdateStatus.getDownloadUrl()

    fun getAppVersion(): String = "${appInfo.versionName} (${appInfo.versionCode})"

    fun navigateToConnectionStats() = router.updateDestination(ConnectionStats)

    fun navigateToDebugLogs() = router.updateDestination(DebugLogs)

    fun navigateToAbout() = router.updateDestination(About)

    fun navigateToPrivacyPolicy() = router.updateDestination(WebDestination.Privacy)
}

@KoinViewModel
class ConnectionStatsViewModel(
    usageEvents: UsageEventsControl,
) : ViewModel() {
    private val _eventList = MutableStateFlow<List<String>>(emptyList())
    val eventList: StateFlow<List<String>> = _eventList.asStateFlow()

    init {
        viewModelScope.launch { _eventList.value = usageEvents.recentEvents() }
    }
}

@KoinViewModel
class DebugLogsViewModel(
    private val getLogs: GetLogsUseCase,
    private val logCleaner: DebugLogCleaner,
) : ViewModel() {
    private val _debugLogs = MutableStateFlow<List<String>>(emptyList())
    val debugLogs: StateFlow<List<String>> = _debugLogs.asStateFlow()

    init {
        viewModelScope.launch { _debugLogs.value = getLogs.getDebugLogs() }
    }

    fun clearDebugLogs() {
        viewModelScope.launch {
            logCleaner.clearDebugLogs()
            _debugLogs.value = emptyList()
        }
    }
}