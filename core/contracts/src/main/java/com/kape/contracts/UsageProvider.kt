package com.kape.contracts

import kotlinx.coroutines.flow.StateFlow

interface UsageProvider {
    val download: StateFlow<String>
    val upload: StateFlow<String>
    val widgetDownloadSpeed: StateFlow<String>
    val widgetDownload: StateFlow<String>
    val widgetUploadSpeed: StateFlow<String>
    val widgetUpload: StateFlow<String>

    fun reset()
}