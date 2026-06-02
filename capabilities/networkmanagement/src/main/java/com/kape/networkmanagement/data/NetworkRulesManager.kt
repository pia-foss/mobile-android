package com.kape.networkmanagement.data

import com.kape.localprefs.prefs.NetworkManagementPrefs
import com.kape.networkmanagement.utils.NetworkUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Singleton

@Singleton
class NetworkRulesManager(
    private val prefs: NetworkManagementPrefs,
    private val util: NetworkUtil,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getRules(): Flow<List<NetworkItem>> =
        prefs.allRules
            .onEach { rules ->
                if (rules.isEmpty()) {
                    util.getDefaultList().forEach {
                        prefs.addDefaultRule(it)
                    }
                }
            }.map { rules ->
                rules.sortedBy { !it.isDefault }
            }

    suspend fun updateRule(
        rule: NetworkItem,
        behavior: NetworkBehavior,
    ) {
        prefs.updateRuleForNetwork(rule, behavior)
    }

    suspend fun addRule(
        ssid: String,
        behavior: NetworkBehavior,
    ) {
        prefs.addRuleForNetwork(ssid, behavior)
    }

    suspend fun removeRule(rule: NetworkItem) {
        prefs.removeRuleForNetwork(rule)
    }
}