package com.kape.appbar.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.utils.ConnectionListener
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppBarViewModel : ViewModel(), KoinComponent {

    private val connectionListener: ConnectionListener by inject()

    var appBarText by mutableStateOf("")
        private set
    var accessibilityPrefix by mutableStateOf("")
        private set

    lateinit var appBarConnectionState: ConnectionListener.ConnectionStatus
        private set

    init {
        viewModelScope.launch {
            connectionListener.connectionStatus.collect {
                refreshConnectionState(it.second!!)
                appBarConnectionState = it.first
            }
        }
    }

    fun appBarText(title: String?) {
        appBarText = title ?: appBarTitle(connectionListener.connectionStatus.value.second!!)
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