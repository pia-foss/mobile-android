package com.kape.appbar.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.ConnectionStatusProvider
import com.kape.contracts.Router
import com.kape.data.ConnectionStatus
import com.kape.data.DI
import com.kape.utils.NetworkConnectionListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
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
) : ViewModel() {
    val isConnected = networkConnectionListener.isConnected

    var appBarText by mutableStateOf("")
        private set
    var accessibilityPrefix by mutableStateOf("")
        private set

    var appBarConnectionState: ConnectionStatus = connectionStatusProvider.state.value.status
        private set

    init {
        viewModelScope.launch(ioDispatcher) {
            connectionStatusProvider.state.collectLatest {
                withContext(mainDispatcher) {
                    refreshConnectionState(it.title)
                    appBarConnectionState = it.status
                }
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

    private fun appBarTitle(status: String): String = status
}