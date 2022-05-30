package com.kape.vpn_permissions.data

import android.content.Context
import android.net.VpnService
import com.kape.vpn_permissions.domain.VpnPermissionDataSource

class VpnPermissionDataSourceImpl(
        private val context: Context
): VpnPermissionDataSource {
    override fun isVpnProfileInstalled(): Boolean = VpnService.prepare(context) == null
}
