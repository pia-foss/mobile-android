package com.kape.login.domain

interface LocalSessionRepository {
    suspend fun isAutomationEnabled(): Boolean

    suspend fun markDisconnectedByUser()

    suspend fun clear()
}