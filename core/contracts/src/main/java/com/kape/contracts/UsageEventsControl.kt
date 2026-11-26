package com.kape.contracts

interface UsageEventsControl {
    fun start()

    fun stop()

    fun flush()

    suspend fun recentEvents(): List<String>
}