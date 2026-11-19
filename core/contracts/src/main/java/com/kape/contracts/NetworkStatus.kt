package com.kape.contracts

import kotlinx.coroutines.flow.StateFlow

interface NetworkStatus {
    val isOnline: StateFlow<Boolean>
}