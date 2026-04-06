package com.kape.appbar.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.ConnectionStatusProvider
import com.kape.contracts.Router
import com.kape.data.ConnectionStatus
import com.kape.utils.NetworkConnectionListener
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AppBarViewModel(
    private val router: Router,
    private val connectionStatusProvider: ConnectionStatusProvider,
    networkConnectionListener: NetworkConnectionListener,
) : ViewModel() {

    val isConnected = networkConnectionListener.isConnected

    var appBarText by mutableStateOf("")
        private set
    var accessibilityPrefix by mutableStateOf("")
        private set

    lateinit var appBarConnectionState: ConnectionStatus
        private set

    init {
        viewModelScope.launch {
            connectionStatusProvider.state.collect {
                refreshConnectionState(it.title)
                appBarConnectionState = it.status
            }
        }
    }

    fun appBarText(title: String?) {
        appBarText = title ?: appBarTitle(connectionStatusProvider.state.value.title)
    }

    fun navigateBack() = router.navigateBack()

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