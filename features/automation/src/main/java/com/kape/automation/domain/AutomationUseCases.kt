package com.kape.automation.domain

import com.kape.contracts.LocationPermissionStatus
import com.kape.localprefs.api.SettingsStore
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory

@Factory
class ObserveAutomationEnabledUseCase(
    private val prefs: SettingsStore,
) {
    operator fun invoke(): StateFlow<Boolean> = prefs.isAutomationEnabled
}

@Factory
class EnableAutomationUseCase(
    private val prefs: SettingsStore,
    private val service: AutomationServiceControl,
    private val network: NetworkObserver,
) {
    suspend operator fun invoke() {
        prefs.setAutomationEnabled(true)
        service.start()
        network.reapplyRules()
    }
}

enum class AutomationToggleResult { Enabled, Disabled, MissingLocationPermission }

@Factory
class ToggleAutomationUseCase(
    private val prefs: SettingsStore,
    private val service: AutomationServiceControl,
    private val permissions: LocationPermissionStatus,
    private val enableAutomation: EnableAutomationUseCase,
) {
    suspend operator fun invoke(): AutomationToggleResult =
        when {
            prefs.isAutomationEnabledNow() -> {
                prefs.setAutomationEnabled(false)
                service.stop()
                AutomationToggleResult.Disabled
            }

            permissions.isFineLocationPermissionGranted() && permissions.isBackgroundLocationPermissionGranted() -> {
                enableAutomation()
                AutomationToggleResult.Enabled
            }

            else -> AutomationToggleResult.MissingLocationPermission
        }
}

@Factory
class ObserveNetworkRulesUseCase(
    private val rules: NetworkRulesRepository,
) {
    operator fun invoke(): Flow<List<NetworkItem>> = rules.getRules()
}

/** Every change re-applies the rules, so the current network follows its new rule straight away. */
@Factory
class EditNetworkRulesUseCase(
    private val rules: NetworkRulesRepository,
    private val network: NetworkObserver,
) {
    suspend fun add(
        ssid: String,
        behavior: NetworkBehavior,
    ) = rules.addRule(ssid, behavior).also { network.reapplyRules() }

    suspend fun update(
        rule: NetworkItem,
        behavior: NetworkBehavior,
    ) = rules.updateRule(rule, behavior).also { network.reapplyRules() }

    suspend fun remove(rule: NetworkItem) = rules.removeRule(rule).also { network.reapplyRules() }
}