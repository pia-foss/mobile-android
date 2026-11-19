package com.kape.contracts

import kotlinx.coroutines.flow.StateFlow

interface AppUpdateStatus {
    val hasUpdateAvailable: StateFlow<Boolean>

    suspend fun getDownloadUrl(): String?
}