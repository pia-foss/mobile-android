package com.kape.automation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.kape.automation.ui.screens.AutomationScreen
import com.kape.automation.ui.viewmodel.AutomationViewModel
import com.kape.automation.ui.screens.BackgroundLocationPermissionScreen
import com.kape.automation.ui.screens.LocationPermissionScreen
import com.kape.automation.utils.AutomationStep
import org.koin.androidx.compose.koinViewModel

@Composable
fun AutomationFlow() {
    val viewModel: AutomationViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()

    when (state) {
        AutomationStep.LocationPermission -> LocationPermissionScreen()
        AutomationStep.EnableBackgroundLocation -> BackgroundLocationPermissionScreen()
        AutomationStep.Main -> AutomationScreen()
    }
}