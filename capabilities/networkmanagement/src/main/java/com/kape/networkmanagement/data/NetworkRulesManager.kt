package com.kape.networkmanagement.data

import com.kape.networkmanagement.NetworkManagementPrefs
import com.kape.networkmanagement.utils.NetworkUtil

class NetworkRulesManager(
    private val prefs: NetworkManagementPrefs,
    private val util: NetworkUtil,
) {

    fun getRules(): List<NetworkItem> {
        if (prefs.getAllRules().isEmpty()) {
            for (item in util.getDefaultList()) {
                prefs.addDefaultRule(item)
            }
        }
        return prefs.getAllRules()
    }

    fun updateRule(rule: NetworkItem, behavior: NetworkBehavior) {
        prefs.updateRuleForNetwork(rule, behavior)
    }
}