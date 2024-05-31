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
        return prefs.getAllRules().sortedBy { !it.isDefault }
    }

    fun updateRule(rule: NetworkItem, behavior: NetworkBehavior) {
        prefs.updateRuleForNetwork(rule, behavior)
    }

    fun addRule(ssid: String, behavior: NetworkBehavior) {
        prefs.addRuleForNetwork(ssid, behavior)
    }

    fun removeRule(rule: NetworkItem) {
        prefs.removeRuleForNetwork(rule)
    }
}