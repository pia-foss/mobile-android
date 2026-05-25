package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.kape.localprefs.Prefs
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.data.NetworkType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

private val NETWORK_RULES = stringSetPreferencesKey("network-rules")

@Singleton
class NetworkManagementPrefs(
    context: Context,
) : Prefs(context, "network-management") {
    fun addRuleForNetwork(
        ssid: String,
        behavior: NetworkBehavior,
    ) {
        scope.launch {
            addNetworkItem(
                NetworkItem(
                    networkName = ssid,
                    networkType = NetworkType.WifiCustom,
                    networkBehavior = behavior,
                ),
            )
        }
    }

    fun addDefaultRule(networkItem: NetworkItem) {
        scope.launch {
            addNetworkItem(networkItem)
        }
    }

    fun removeRuleForNetwork(rule: NetworkItem) {
        scope.launch {
            dataStore.edit { prefs ->
                val current = prefs[NETWORK_RULES]?.toMutableSet() ?: mutableSetOf()
                current.remove(Json.encodeToString(rule))
                prefs[NETWORK_RULES] = current
            }
        }
    }

    fun updateRuleForNetwork(
        rule: NetworkItem,
        behavior: NetworkBehavior,
    ) {
        val updatedRule =
            NetworkItem(
                networkName = rule.networkName,
                networkBehavior = behavior,
                networkType = rule.networkType,
                isDefaultForMobile = rule.isDefaultForMobile,
                isDefaultForOpen = rule.isDefaultForOpen,
                isDefaultForSecure = rule.isDefaultForSecure,
            )
        scope.launch {
            dataStore.edit { prefs ->
                val current = prefs[NETWORK_RULES]?.toMutableSet() ?: mutableSetOf()
                current.remove(Json.encodeToString(rule))
                current.add(Json.encodeToString(updatedRule))
                prefs[NETWORK_RULES] = current
            }
        }
    }

    fun getAllRules(): Flow<List<NetworkItem>> =
        dataStore.data.map { prefs ->
            prefs[NETWORK_RULES]?.map { Json.decodeFromString(it) } ?: emptyList()
        }

    fun getRuleForNetwork(ssid: String): Flow<NetworkItem?> =
        dataStore.data.map { prefs ->
            prefs[NETWORK_RULES]
                ?.map { Json.decodeFromString<NetworkItem>(it) }
                ?.firstOrNull { it.networkName == ssid }
        }

    private suspend fun addNetworkItem(item: NetworkItem) {
        dataStore.edit { prefs ->
            val current = prefs[NETWORK_RULES] ?: emptySet()
            prefs[NETWORK_RULES] = current + Json.encodeToString(item)
        }
    }
}