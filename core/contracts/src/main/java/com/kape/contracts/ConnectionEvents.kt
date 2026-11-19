package com.kape.contracts

import com.kape.data.kpi.KpiConnectionStatus

fun interface ConnectionEvents {
    suspend fun connectionStatusChanged(
        status: KpiConnectionStatus,
        isManual: Boolean,
    )
}