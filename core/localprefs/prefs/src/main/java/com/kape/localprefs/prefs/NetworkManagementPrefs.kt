package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.kape.contracts.di.Qualifiers
import com.kape.localprefs.Prefs
import com.kape.localprefs.api.NetworkManagementStore
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.data.NetworkType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

private val NETWORK_RULES = stringSetPreferencesKey("network-rules")

@Singleton(binds = [NetworkManagementPrefs::class, NetworkManagementStore::class])
class NetworkManagementPrefs(
    context: Context,
    @Named(Qualifiers.APP_SCOPE) appScope: CoroutineScope,
    @Named(Qualifiers.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
) : Prefs(context, "network-management", appScope, ioDispatcher),
    NetworkManagementStore {
    val allRules: StateFlow<List<NetworkItem>> =
        getAllRules().stateIn(scope, SharingStarted.WhileSubscribed(waitTime), emptyList())

    suspend fun addRuleForNetwork(
        ssid: String,
        behavior: NetworkBehavior,
    ) {
        addNetworkItem(
            NetworkItem(
                networkName = ssid,
                networkType = NetworkType.WifiCustom,
                networkBehavior = behavior,
            ),
        )
    }

    suspend fun addDefaultRule(networkItem: NetworkItem) {
        addNetworkItem(networkItem)
    }

    suspend fun removeRuleForNetwork(rule: NetworkItem) {
        dataStore.edit { prefs ->
            val current = prefs[NETWORK_RULES]?.toMutableSet() ?: mutableSetOf()
            current.remove(Json.encodeToString(rule))
            prefs[NETWORK_RULES] = current
        }
    }

    suspend fun updateRuleForNetwork(
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
        dataStore.edit { prefs ->
            val current = prefs[NETWORK_RULES]?.toMutableSet() ?: mutableSetOf()
            current.remove(Json.encodeToString(rule))
            current.add(Json.encodeToString(updatedRule))
            prefs[NETWORK_RULES] = current
        }
    }

    override suspend fun getAllRulesNow(): List<NetworkItem> = getAllRules().first()

    fun getRuleForNetwork(ssid: String): Flow<NetworkItem?> =
        dataStore.data.map { prefs ->
            prefs[NETWORK_RULES]
                ?.map { Json.decodeFromString<NetworkItem>(it) }
                ?.firstOrNull { it.networkName == ssid }
        }

    private fun getAllRules(): Flow<List<NetworkItem>> =
        dataStore.data.map { prefs ->
            prefs[NETWORK_RULES]?.map { Json.decodeFromString(it) } ?: emptyList()
        }

    private suspend fun addNetworkItem(item: NetworkItem) {
        dataStore.edit { prefs ->
            val current = prefs[NETWORK_RULES] ?: emptySet()
            prefs[NETWORK_RULES] = current + Json.encodeToString(item)
        }
    }
}