package com.kape.contracts

import com.kape.data.ConnectionStatus

fun interface ConnectionEvents {
    suspend fun connectionStatusChanged(
        status: ConnectionStatus,
        isManual: Boolean,
    )
}