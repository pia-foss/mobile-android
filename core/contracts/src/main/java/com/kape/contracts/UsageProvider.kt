package com.kape.contracts

import kotlinx.coroutines.flow.StateFlow

interface UsageProvider {
    val download: StateFlow<String>
    val upload: StateFlow<String>
    val widgetDownloadSpeed: StateFlow<String>
    val widgetDownload: StateFlow<String>
    val widgetUploadSpeed: StateFlow<String>
    val widgetUpload: StateFlow<String>

    /** Raw cumulative byte counters for the current tunnel session; reset to zero on disconnect. */
    val tunnelTraffic: StateFlow<TunnelTraffic>

    fun byteCount(
        tx: Long,
        rx: Long,
    )

    fun reset()
}

data class TunnelTraffic(
    val sent: Long,
    val received: Long,
) {
    companion object {
        val ZERO = TunnelTraffic(0, 0)
    }
}