package com.kape.automation.data

import com.kape.automation.domain.NetworkRulesRepository
import com.kape.localprefs.prefs.NetworkManagementPrefs
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.utils.NetworkUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.Singleton

@Singleton(binds = [NetworkRulesRepository::class])
class NetworkRulesManager(
    private val prefs: NetworkManagementPrefs,
    private val util: NetworkUtil,
) : NetworkRulesRepository {
    // allRules starts as an empty list on every fresh subscription, so the stored rules decide whether to seed the defaults.
    override fun getRules(): Flow<List<NetworkItem>> =
        prefs.allRules
            .onStart {
                if (prefs.getAllRulesNow().isEmpty()) {
                    util.getDefaultList().forEach {
                        prefs.addDefaultRule(it)
                    }
                }
            }.map { rules ->
                rules.sortedBy { !it.isDefault }
            }

    override suspend fun updateRule(
        rule: NetworkItem,
        behavior: NetworkBehavior,
    ) {
        prefs.updateRuleForNetwork(rule, behavior)
    }

    override suspend fun addRule(
        ssid: String,
        behavior: NetworkBehavior,
    ) {
        val existingRule = prefs.getAllRulesNow().firstOrNull { it.networkName == ssid }
        existingRule?.let {
            prefs.removeRuleForNetwork(existingRule)
        }
        prefs.addRuleForNetwork(ssid, behavior)
    }

    override suspend fun removeRule(rule: NetworkItem) {
        prefs.removeRuleForNetwork(rule)
    }
}