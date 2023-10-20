package com.kape.permissions.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.kape.permissions.ui.vm.PermissionsViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun NotificationPermissionScreen() {
    val viewModel: PermissionsViewModel = getViewModel()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            viewModel.exitOnboarding()
        },
    )

    LaunchedEffect(key1 = Unit) {
        if (!viewModel.isNotificationPermissionGranted()) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.exitOnboarding()
        }
    }
}