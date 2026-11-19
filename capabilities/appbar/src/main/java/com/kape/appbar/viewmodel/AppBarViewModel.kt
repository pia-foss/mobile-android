package com.kape.appbar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.ConnectionStatusSource
import com.kape.contracts.NetworkStatus
import com.kape.contracts.Router
import com.kape.contracts.SnoozeStatus
import com.kape.data.ConnectionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AppBarViewModel(
    private val router: Router,
    private val connection: ConnectionStatusSource,
    networkStatus: NetworkStatus,
    snoozeStatus: SnoozeStatus,
) : ViewModel() {
    val isConnected: StateFlow<Boolean> = networkStatus.isOnline
    val connectionStatus: StateFlow<ConnectionStatus> = connection.status

    private val customTitle = MutableStateFlow<String?>(null)
    private val _currentTitle = MutableStateFlow("")
    val currentTitle: StateFlow<String> = _currentTitle.asStateFlow()

    private val _snoozeCountdown = MutableStateFlow<String?>(null)

    val snoozeCountdown: StateFlow<String?> = _snoozeCountdown.asStateFlow()

    init {
        viewModelScope.launch {
            connection.title.collect { title -> _currentTitle.value = customTitle.value ?: title }
        }
        viewModelScope.launch {
            snoozeStatus.snoozeEndTime.collectLatest { end ->
                var remaining = end - System.currentTimeMillis()
                while (end > 0L && remaining > 0L) {
                    _snoozeCountdown.value = formatCountdown(remaining)
                    delay((remaining - 1) % 1_000L + 1)
                    remaining = end - System.currentTimeMillis()
                }
                _snoozeCountdown.value = null
            }
        }
    }

    fun appBarText(title: String?) {
        customTitle.value = title
        _currentTitle.value = title ?: connection.title.value
    }

    fun navigateBack() = router.navigateBack()
}

internal fun formatCountdown(remainingMillis: Long): String {
    val totalSeconds = (remainingMillis + 999) / 1_000
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
}