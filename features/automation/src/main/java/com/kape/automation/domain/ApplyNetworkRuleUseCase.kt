package com.kape.automation.domain

import com.kape.contracts.ConnectionStatusSource
import com.kape.data.ConnectionStatus
import com.kape.localprefs.api.ConnectionStore
import com.kape.localprefs.api.NetworkManagementStore
import com.kape.localprefs.api.SettingsStore
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.vpnlauncher.VpnLauncher
import org.koin.core.annotation.Factory

/** Defaults are found by their flag, not their name, which is translated and so changes with the language. */
internal fun ruleFor(
    rules: List<NetworkItem>,
    ssid: String,
    isWifi: Boolean,
): NetworkItem? =
    rules.firstOrNull { it.networkName == ssid && ssid.isNotEmpty() }
        ?: rules.firstOrNull { if (isWifi) it.isDefaultForOpen else it.isDefaultForMobile }

enum class RuleAction { Connect, Disconnect, Nothing }

internal fun actionFor(
    behavior: NetworkBehavior,
    status: ConnectionStatus,
): RuleAction =
    when (behavior) {
        is NetworkBehavior.AlwaysConnect ->
            if (status == ConnectionStatus.DISCONNECTED ||
                status == ConnectionStatus.DISCONNECTING
            ) {
                RuleAction.Connect
            } else {
                RuleAction.Nothing
            }

        is NetworkBehavior.AlwaysDisconnect -> RuleAction.Disconnect
        is NetworkBehavior.RetainState -> RuleAction.Nothing
    }

@Factory
class ApplyNetworkRuleUseCase(
    private val settingsPrefs: SettingsStore,
    private val connectionPrefs: ConnectionStore,
    private val networkPrefs: NetworkManagementStore,
    private val connectionStatus: ConnectionStatusSource,
    private val vpnLauncher: VpnLauncher,
) {
    suspend operator fun invoke(
        ssid: String,
        isWifi: Boolean,
        isNetworkChange: Boolean,
    ) {
        if (!settingsPrefs.isAutomationEnabledNow()) return
        // Read the stored end time, as the snooze itself may not be restored yet after the app was killed.
        if (connectionPrefs.getLastSnoozeEndTimeNow() > System.currentTimeMillis()) return
        if (isNetworkChange) connectionPrefs.setDisconnectedByUser(false)

        val rule = ruleFor(networkPrefs.getAllRulesNow(), ssid, isWifi) ?: return
        when (actionFor(rule.networkBehavior, connectionStatus.status.value)) {
            RuleAction.Connect -> vpnLauncher.launchVpn()
            RuleAction.Disconnect -> vpnLauncher.stopVpn()
            RuleAction.Nothing -> Unit
        }
    }
}