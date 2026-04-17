package com.kape.contracts

import com.kape.data.VpnConnectionStatus
import kotlinx.coroutines.flow.StateFlow

interface ConnectionStatusProvider {
    val state: StateFlow<VpnConnectionStatus>
}