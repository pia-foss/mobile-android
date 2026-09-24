package com.kape.appbar.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.ConnectionStatusProvider
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.snooze.SnoozeHandler
import com.kape.utils.NetworkConnectionListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class AppBarViewModel(
    private val router: Router,
    private val connectionStatusProvider: ConnectionStatusProvider,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
    @Named(DI.MAIN_DISPATCHER) private val mainDispatcher: CoroutineDispatcher,
    networkConnectionListener: NetworkConnectionListener,
    snoozeHandler: SnoozeHandler,
) : ViewModel() {
    val isConnected = networkConnectionListener.isConnected
    val connectionStatus = connectionStatusProvider.status
    private val _currentTitle = MutableStateFlow("")
    val currentTitle = _currentTitle.asStateFlow()
    private val connectionTitle = connectionStatusProvider.title

    // Remaining snooze time formatted as m:ss / h:mm:ss, or null when no snooze is active.
    private val _snoozeCountdown = MutableStateFlow<String?>(null)
    val snoozeCountdown = _snoozeCountdown.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            connectionTitle.collectLatest { title ->
                withContext(mainDispatcher) {
                    _currentTitle.update { title }
                }
            }
        }
        viewModelScope.launch(ioDispatcher) {
            snoozeHandler.snoozeEndTime.collectLatest { end ->
                var remaining = end - System.currentTimeMillis()
                while (end > 0L && remaining > 0L) {
                    _snoozeCountdown.update { formatCountdown(remaining) }
                    // Wake up exactly when the displayed second changes.
                    delay((remaining - 1) % 1_000L + 1)
                    remaining = end - System.currentTimeMillis()
                }
                _snoozeCountdown.update { null }
            }
        }
    }

    fun appBarText(title: String?) {
        _currentTitle.update { title ?: connectionTitle.value }
    }

    fun navigateBack() = router.navigateBack()

    private fun formatCountdown(remainingMillis: Long): String {
        // Round up so the countdown never shows 0:00 while still snoozed.
        val totalSeconds = (remainingMillis + 999) / 1_000
        val hours = totalSeconds / 3_600
        val minutes = (totalSeconds % 3_600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%d:%02d".format(minutes, seconds)
        }
    }
}