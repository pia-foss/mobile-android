package com.kape.permissions.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.kape.permissions.ui.NotificationPermissionScreen
import com.kape.permissions.ui.VpnPermissionScreen
import com.kape.permissions.ui.vm.PermissionsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PermissionsFlow() {
    val viewModel: PermissionsViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()

    when (state) {
        PermissionsStep.Notifications -> NotificationPermissionScreen()
        PermissionsStep.Vpn -> VpnPermissionScreen()
    }
}