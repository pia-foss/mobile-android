package com.kape.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import androidx.core.content.ContextCompat
import com.kape.contracts.PermissionStatus
import org.koin.core.annotation.Singleton

@Singleton(binds = [PermissionStatus::class])
internal class AndroidPermissionStatus(
    private val context: Context,
) : PermissionStatus {
    // prepare() throws IllegalStateException on some devices whose VPN framework is unavailable.
    override fun isVpnProfileInstalled(): Boolean =
        try {
            VpnService.prepare(context) == null
        } catch (_: IllegalStateException) {
            false
        }

    override fun areNotificationsAllowed(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}