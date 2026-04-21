package com.kape.automation.di

import android.content.Intent
import com.kape.automation.ui.viewmodel.AutomationViewModel
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.location.data.LocationPermissionManager
import com.kape.networkmanagement.data.NetworkRulesManager
import com.kape.utils.AutomationManager
import com.kape.utils.NetworkConnectionListener
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named

@Module
class AutomationModule {
    @KoinViewModel
    fun provideAutomationViewModel(
        router: Router,
        locationPermissionManager: LocationPermissionManager,
        settingsPrefs: SettingsPrefs,
        networkRulesManager: NetworkRulesManager,
        networkConnectionListener: NetworkConnectionListener,
        @Named(DI.RULES_UPDATED_INTENT) broadcastIntent: Intent,
        automationManager: AutomationManager,
    ): AutomationViewModel =
        AutomationViewModel(
            router,
            locationPermissionManager,
            settingsPrefs,
            networkRulesManager,
            networkConnectionListener,
            broadcastIntent,
            automationManager,
        )
}