package com.kape.contracts

import com.kape.data.ConnectionStatus
import kotlinx.coroutines.flow.StateFlow

interface ConnectionStatusSource {
    val status: StateFlow<ConnectionStatus>

    val title: StateFlow<String>
}