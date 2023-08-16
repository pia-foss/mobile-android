package com.kape.notifications.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class NotificationPermissionManager(private val context: Context) {
    fun isNotificationsPermissionGranted() =
        Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED

}