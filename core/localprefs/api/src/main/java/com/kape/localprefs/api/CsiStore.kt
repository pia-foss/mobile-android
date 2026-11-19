package com.kape.localprefs.api

interface CsiStore {
    suspend fun clearProtocolDebugLogs()

    suspend fun setProtocolDebugLogs(value: String)
}