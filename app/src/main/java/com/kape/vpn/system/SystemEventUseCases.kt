package com.kape.vpn.system

import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.IsUserLoggedInUseCase
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpn.automation.StartAutomationIfEnabledUseCase
import com.kape.vpnlauncher.VpnLauncher
import org.koin.core.annotation.Factory

/** A snooze survives a reboot (its worker resumes the VPN when it ends), so it isn't cut short. */
@Factory
class OnBootUseCase(
    private val settingsPrefs: SettingsPrefs,
    private val connectionPrefs: ConnectionPrefs,
    private val startAutomationIfEnabled: StartAutomationIfEnabledUseCase,
    private val vpnLauncher: VpnLauncher,
) {
    suspend operator fun invoke() {
        startAutomationIfEnabled()
        val isSnoozed = connectionPrefs.getLastSnoozeEndTimeNow() > System.currentTimeMillis()
        if (settingsPrefs.isLaunchOnBootEnabledNow() && !isSnoozed) vpnLauncher.launchVpn()
    }
}

@Factory
class OnAppUpdatedUseCase(
    private val settingsPrefs: SettingsPrefs,
    private val startAutomationIfEnabled: StartAutomationIfEnabledUseCase,
    private val vpnLauncher: VpnLauncher,
) {
    suspend operator fun invoke() {
        startAutomationIfEnabled()
        // The login check happens, with retries, when connecting, since right after an update a
        // single check here can briefly see the user as logged out.
        if (settingsPrefs.isConnectOnAppUpdateEnabledNow()) vpnLauncher.launchVpn()
    }
}

sealed interface VpnTile {
    data object LoggedOut : VpnTile

    data object Disconnected : VpnTile

    data class Connected(
        val serverName: String,
    ) : VpnTile
}

@Factory
class GetVpnTileUseCase(
    private val connectionInfo: ConnectionInfoProvider,
    private val isUserLoggedIn: IsUserLoggedInUseCase,
) {
    suspend operator fun invoke(): VpnTile =
        when {
            connectionInfo.isConnected() -> VpnTile.Connected(connectionInfo.name)
            !isUserLoggedIn.invoke() -> VpnTile.LoggedOut
            else -> VpnTile.Disconnected
        }
}

@Factory
class ToggleVpnFromTileUseCase(
    private val connectionInfo: ConnectionInfoProvider,
    private val isUserLoggedIn: IsUserLoggedInUseCase,
    private val vpnLauncher: VpnLauncher,
) {
    suspend operator fun invoke() {
        if (!isUserLoggedIn.invoke()) return
        if (connectionInfo.isConnected()) vpnLauncher.stopVpn() else vpnLauncher.launchVpn()
    }
}