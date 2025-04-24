package com.kape.networkmanagement.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kape.networkmanagement.NetworkManagementPrefs
import com.kape.networkmanagement.utils.NetworkUtil

class NetworkRulesManager(
    private val prefs: NetworkManagementPrefs,
    private val util: NetworkUtil,
) {

    var networkRules by mutableStateOf(getRules())
        private set

    private fun getRules(): List<NetworkItem> {
        if (prefs.getAllRules().isEmpty()) {
            for (item in util.getDefaultList()) {
                prefs.addDefaultRule(item)
            }
        }
        return prefs.getAllRules().sortedBy { !it.isDefault }
    }

    fun updateRule(rule: NetworkItem, behavior: NetworkBehavior) {
        prefs.updateRuleForNetwork(rule, behavior)
        networkRules = getRules()
    }

    fun addRule(ssid: String, behavior: NetworkBehavior) {
        prefs.addRuleForNetwork(ssid, behavior)
        networkRules = getRules()
    }

    fun removeRule(rule: NetworkItem) {
        prefs.removeRuleForNetwork(rule)
        networkRules = getRules()
    }
}