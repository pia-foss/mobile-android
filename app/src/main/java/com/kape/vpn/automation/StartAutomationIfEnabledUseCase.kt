package com.kape.vpn.automation

import com.kape.contracts.AutomationServiceLauncher
import com.kape.localprefs.prefs.SettingsPrefs
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Singleton

fun interface AutomationSettingsRepository {
    suspend fun isAutomationEnabled(): Boolean
}

@Factory
class StartAutomationIfEnabledUseCase(
    private val settings: AutomationSettingsRepository,
    private val launcher: AutomationServiceLauncher,
) {
    suspend operator fun invoke() {
        if (settings.isAutomationEnabled()) launcher.start()
    }
}

@Singleton(binds = [AutomationSettingsRepository::class])
internal class PrefsAutomationSettingsRepository(
    private val prefs: SettingsPrefs,
) : AutomationSettingsRepository {
    override suspend fun isAutomationEnabled(): Boolean = prefs.isAutomationEnabledNow()
}