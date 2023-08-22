package com.kape.vpnpermission.data

import android.content.Context
import android.net.VpnService
import com.kape.vpnpermission.domain.VpnPermissionDataSource

class VpnPermissionDataSourceImpl(
    private val context: Context,
) : VpnPermissionDataSource {
    override fun isVpnProfileInstalled(): Boolean = VpnService.prepare(context) == null
}