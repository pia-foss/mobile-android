package com.kape.vpn.provider

import com.kape.csi.CsiPrefs
import com.kape.settings.SettingsPrefs
import com.kape.vpnmanager.presenters.VPNManagerDebugLoggingDependency
import com.kape.vpnmanager.presenters.VPNManagerPermissionsDependency
import com.kape.vpnmanager.presenters.VPNManagerResultCallback

class VpnManagerProvider(private val prefs: CsiPrefs, private val settingsPrefs: SettingsPrefs) :
    VPNManagerPermissionsDependency,
    VPNManagerDebugLoggingDependency {
    override fun debugLog(log: String) {
        prefs.addCustomDebugLogs("vpn debug log: $log", settingsPrefs.isDebugLoggingEnabled())
    }

    override fun requestNecessaryPermissions(callback: VPNManagerResultCallback<Boolean>) {
        callback.invoke(Result.success(false))
    }
}