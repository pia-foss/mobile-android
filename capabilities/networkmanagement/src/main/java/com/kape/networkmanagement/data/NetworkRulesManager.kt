package com.kape.networkmanagement.data

import com.kape.networkmanagement.NetworkManagementPrefs
import com.kape.networkmanagement.utils.NetworkUtil

class NetworkRulesManager(
    private val prefs: NetworkManagementPrefs,
    private val util: NetworkUtil,
) {

    fun getRules(): List<NetworkItem> {
        return prefs.getAllRules().ifEmpty {
            util.getDefaultList()
        }
    }

    fun updateRule(rule: NetworkItem, behavior: NetworkBehavior) {
        prefs.updateRuleForNetwork(rule, behavior)
    }
}