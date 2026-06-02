package com.kape.vpn.provider

import com.kape.data.DI
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpnmanager.presenters.VPNManagerDebugLoggingDependency
import com.kape.vpnmanager.presenters.VPNManagerPermissionsDependency
import com.kape.vpnmanager.presenters.VPNManagerResultCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Singleton
class VpnManagerProvider(
    private val prefs: CsiPrefs,
    private val settingsPrefs: SettingsPrefs,
    @Named(DI.IO_SCOPE) private val ioScope: CoroutineScope,
) : VPNManagerPermissionsDependency,
    VPNManagerDebugLoggingDependency {
    override fun debugLog(log: String) {
        ioScope.launch {
            prefs.addCustomDebugLogs("vpn debug log: $log", settingsPrefs.isDebugLoggingEnabled.first())
        }
    }

    override fun requestNecessaryPermissions(callback: VPNManagerResultCallback<Boolean>) {
        callback.invoke(Result.success(false))
    }
}