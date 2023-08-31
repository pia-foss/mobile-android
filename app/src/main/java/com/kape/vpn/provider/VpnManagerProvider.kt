package com.kape.vpn.provider

import com.kape.vpnmanager.presenters.VPNManagerDebugLoggingDependency
import com.kape.vpnmanager.presenters.VPNManagerPermissionsDependency
import com.kape.vpnmanager.presenters.VPNManagerResultCallback

class VpnManagerProvider : VPNManagerPermissionsDependency,
    VPNManagerDebugLoggingDependency {
    override fun debugLog(log: String) {
        // TODO: implement?
    }

    override fun requestNecessaryPermissions(callback: VPNManagerResultCallback<Boolean>) {
        callback.invoke(Result.success(false))
    }
}