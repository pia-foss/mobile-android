package com.kape.networkmanagement

import android.content.Context
import android.net.wifi.ScanResult
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.data.NetworkType
import com.kape.utils.Prefs
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val NETWORK_RULES = "network-rules"

class NetworkManagementPrefs(context: Context) : Prefs(context, "network-management") {

    fun addRuleForNetwork(network: ScanResult, behavior: NetworkBehavior) {
        val rule = NetworkItem(
            networkName = network.BSSID,
            networkType = NetworkType.WifiCustom,
            networkBehavior = behavior,
        )
        addNetworkItem(rule)
    }

    fun removeRuleForNetwork(rule: NetworkItem) {
        val newRules = getRules().toMutableList()
        newRules.remove(Json.encodeToString(rule))
        prefs.edit().putStringSet(NETWORK_RULES, newRules.toSet()).apply()
    }

    fun updateRuleForNetwork(rule: NetworkItem, behavior: NetworkBehavior) {
        val updatedRule = NetworkItem(
            networkName = rule.networkName,
            networkBehavior = behavior,
            networkType = rule.networkType,
            isDefaultForMobile = rule.isDefaultForMobile,
            isDefaultForOpen = rule.isDefaultForOpen,
            isDefaultForSecure = rule.isDefaultForSecure,
        )
        removeRuleForNetwork(rule)
        addNetworkItem(updatedRule)
    }

    fun getAllRules(): List<NetworkItem> {
        val rules = mutableListOf<NetworkItem>()
        getRules().forEach {
            rules.add(Json.decodeFromString(it))
        }
        return rules
    }

    private fun addNetworkItem(item: NetworkItem) {
        val newRules = mutableSetOf<String>()
        newRules.addAll(getRules())
        newRules.add(Json.encodeToString(item))
        prefs.edit().putStringSet(NETWORK_RULES, newRules).apply()
    }

    private fun getRules() = prefs.getStringSet(NETWORK_RULES, emptySet()) ?: emptySet()
}