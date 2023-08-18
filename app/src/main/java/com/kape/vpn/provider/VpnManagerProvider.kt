package com.kape.vpn.provider

import com.kape.vpnmanager.presenters.VPNManagerDebugLoggingDependency
import com.kape.vpnmanager.presenters.VPNManagerPermissionsDependency
import com.kape.vpnmanager.presenters.VPNManagerProtocolByteCountDependency
import com.kape.vpnmanager.presenters.VPNManagerResultCallback

class VpnManagerProvider : VPNManagerPermissionsDependency, VPNManagerProtocolByteCountDependency,
    VPNManagerDebugLoggingDependency {
    override fun debugLog(log: String) {
        // TODO: implement?
    }

    override fun requestNecessaryPermissions(callback: VPNManagerResultCallback<Boolean>) {
        // TODO: implement
        callback.invoke(Result.success(false))
    }

    override fun byteCount(tx: Long, rx: Long) {
        // TODO: implement
    }
}