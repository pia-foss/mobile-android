package com.kape.appbar.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.utils.NetworkConnectionListener
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class AppBarViewModel(
    private val connectionManager: ConnectionManager,
    networkConnectionListener: NetworkConnectionListener,
) : ViewModel(), KoinComponent {

    val isConnected = networkConnectionListener.isConnected

    var appBarText by mutableStateOf("")
        private set
    var accessibilityPrefix by mutableStateOf("")
        private set

    lateinit var appBarConnectionState: ConnectionStatus
        private set

    init {
        viewModelScope.launch {
            connectionManager.connectionStatusTitle.collect {
                refreshConnectionState(it)
                appBarConnectionState = connectionManager.connectionStatus.value
            }
        }
    }

    fun appBarText(title: String?) {
        appBarText = title ?: appBarTitle(connectionManager.connectionStatusTitle.value)
    }

    private fun refreshAppBarTitle(status: String) {
        appBarText = appBarTitle(status)
    }

    private fun refreshConnectionState(status: String) {
        refreshAppBarTitle(status)
    }

    private fun appBarTitle(status: String): String {
        return status
    }
}