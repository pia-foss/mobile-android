package com.kape.contracts

interface DebugLogCleaner {
    fun scheduleClearOnSessionEnd()

    suspend fun clearDebugLogs()
}