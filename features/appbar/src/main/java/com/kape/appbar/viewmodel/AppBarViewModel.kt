package com.kape.appbar.viewmodel

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.appbar.R
import com.kape.utils.ConnectionListener
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppBarViewModel : ViewModel(), KoinComponent {

    private val connectionListener: ConnectionListener by inject()

    var appBarText by mutableStateOf(R.string.none)
        private set
    var accessibilityPrefix by mutableStateOf(R.string.none)
        private set

    var appBarConnectionState: ConnectionListener.ConnectionStatus by mutableStateOf(
        connectionListener.connectionStatus.value
    )
        private set

    init {
        viewModelScope.launch {
            connectionListener.connectionStatus.collect {
                refreshConnectionState(it)
            }
        }
    }

    fun appBarText(titleId: Int?) {
        appBarText = titleId ?: appBarTitle(connectionListener.connectionStatus.value)
    }

    private fun refreshAppBarTitle(status: ConnectionListener.ConnectionStatus) {
        appBarText = appBarTitle(status)
    }

    private fun refreshConnectionState(status: ConnectionListener.ConnectionStatus) {
        refreshAppBarTitle(status)
        appBarConnectionState = connectionListener.connectionStatus.value
    }

    private fun appBarTitle(status: ConnectionListener.ConnectionStatus): Int {
        return when (status) {
            ConnectionListener.ConnectionStatus.CONNECTED -> R.string.connected_to_format
            ConnectionListener.ConnectionStatus.CONNECTING -> R.string.connecting
            ConnectionListener.ConnectionStatus.DISCONNECTED -> R.string.none
            ConnectionListener.ConnectionStatus.RECONNECTING -> R.string.reconnecting
        }
    }
}