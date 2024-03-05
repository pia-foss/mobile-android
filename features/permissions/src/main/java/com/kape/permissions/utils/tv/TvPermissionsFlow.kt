package com.kape.permissions.utils.tv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.kape.permissions.ui.tv.TvVpnPermissionScreen
import com.kape.permissions.ui.vm.PermissionsViewModel
import com.kape.permissions.utils.PermissionsStep
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvPermissionsFlow() {
    val viewModel: PermissionsViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()

    when (state) {
        PermissionsStep.Notifications -> TODO()
        PermissionsStep.Vpn -> TvVpnPermissionScreen()
        PermissionsStep.Granted -> viewModel.exitOnboarding()
    }
}