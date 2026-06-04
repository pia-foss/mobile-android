package com.kape.appbar.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.ConnectionStatusProvider
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.utils.NetworkConnectionListener
import kotlinx.coroutines.CoroutineDispatcher
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
) : ViewModel() {
    val isConnected = networkConnectionListener.isConnected
    val connectionStatus = connectionStatusProvider.status
    private val _currentTitle = MutableStateFlow("")
    val currentTitle = _currentTitle.asStateFlow()
    private val connectionTitle = connectionStatusProvider.title

    init {
        viewModelScope.launch(ioDispatcher) {
            connectionTitle.collectLatest { title ->
                withContext(mainDispatcher) {
                    _currentTitle.update { title }
                }
            }
        }
    }

    fun appBarText(title: String?) {
        _currentTitle.update { title ?: connectionTitle.value }
    }

    fun navigateBack() = router.navigateBack()
}