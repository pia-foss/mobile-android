package com.kape.networkmanagement.data

import com.kape.localprefs.prefs.NetworkManagementPrefs
import com.kape.networkmanagement.utils.NetworkUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import org.koin.core.annotation.Singleton

@Singleton
class NetworkRulesManager(
    private val prefs: NetworkManagementPrefs,
    private val util: NetworkUtil,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getRules(): Flow<List<NetworkItem>> =
        prefs.allRules.mapLatest { rules ->
            if (rules.isEmpty()) {
                val defaults = util.getDefaultList()

                defaults.forEach { item ->
                    prefs.addDefaultRule(item)
                }

                defaults.sortedBy { !it.isDefault }
            } else {
                rules.sortedBy { !it.isDefault }
            }
        }

    fun updateRule(
        rule: NetworkItem,
        behavior: NetworkBehavior,
    ) {
        prefs.updateRuleForNetwork(rule, behavior)
    }

    fun addRule(
        ssid: String,
        behavior: NetworkBehavior,
    ) {
        prefs.addRuleForNetwork(ssid, behavior)
    }

    fun removeRule(rule: NetworkItem) {
        prefs.removeRuleForNetwork(rule)
    }
}