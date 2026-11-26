package com.kape.settings.ui.vm

import androidx.lifecycle.ViewModel
import com.kape.contracts.Router
import com.kape.data.AutomationSettings
import com.kape.data.GeneralSettings
import com.kape.data.HelpSettings
import com.kape.data.NetworkSettings
import com.kape.data.ObfuscationSettings
import com.kape.data.PrivacySettings
import com.kape.data.ProtocolSettings
import com.kape.localprefs.api.SettingsStore
import com.kape.settings.data.VpnProtocols
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SettingsMenuViewModel(
    private val router: Router,
    prefs: SettingsStore,
) : ViewModel() {
    val selectedProtocol: StateFlow<VpnProtocols> = prefs.selectedProtocol
    val isAutomationEnabled: StateFlow<Boolean> = prefs.isAutomationEnabled

    fun navigateToGeneralSettings() = router.updateDestination(GeneralSettings)

    fun navigateToProtocolSettings() = router.updateDestination(ProtocolSettings)

    fun navigateToNetworkSettings() = router.updateDestination(NetworkSettings)

    fun navigateToPrivacySettings() = router.updateDestination(PrivacySettings)

    fun navigateToHelpSettings() = router.updateDestination(HelpSettings)

    fun navigateToAutomation() = router.updateDestination(AutomationSettings)

    fun navigateToObfuscationSettings() = router.updateDestination(ObfuscationSettings)
}