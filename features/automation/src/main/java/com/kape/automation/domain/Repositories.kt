package com.kape.automation.domain

import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface NetworkRulesRepository {
    fun getRules(): Flow<List<NetworkItem>>

    suspend fun updateRule(
        rule: NetworkItem,
        behavior: NetworkBehavior,
    )

    suspend fun addRule(
        ssid: String,
        behavior: NetworkBehavior,
    )

    suspend fun removeRule(rule: NetworkItem)
}

interface NetworkObserver {
    val ssid: StateFlow<String?>

    fun reapplyRules()
}

interface AutomationServiceControl {
    fun start()

    fun stop()
}