package com.kape.splash.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.ConnectionManager
import com.kape.splash.domain.usecase.GetUpdateUrlUseCase
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class UpdateViewModel(
    private val getUpdateUrl: GetUpdateUrlUseCase,
    private val vpnConnection: ConnectionManager,
) : ViewModel() {
    fun isConnected(): Boolean = vpnConnection.isConnected()

    fun onUpdateClicked(launchUpdate: (url: String) -> Unit) {
        viewModelScope.launch {
            if (vpnConnection.isConnected() || vpnConnection.isConnectionInProgress()) {
                vpnConnection.disconnect()
            }
            getUpdateUrl()?.let(launchUpdate)
        }
    }
}