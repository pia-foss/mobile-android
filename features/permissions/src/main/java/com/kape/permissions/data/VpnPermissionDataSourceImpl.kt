package com.kape.permissions.data

import android.content.Context
import android.net.VpnService
import com.kape.permissions.domain.VpnPermissionDataSource

class VpnPermissionDataSourceImpl(
    private val context: Context,
) : VpnPermissionDataSource {
    override fun isVpnProfileInstalled(): Boolean {
        return try {
            VpnService.prepare(context) == null
        } catch (ex: IllegalStateException) {
            false
        }
    }
}