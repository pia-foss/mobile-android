package com.kape.automation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.kape.automation.ui.AutomationViewModel
import com.kape.automation.ui.LocationPermissionScreen
import com.kape.automation.utils.AutomationStep
import org.koin.androidx.compose.koinViewModel

@Composable
fun AutomationFlow() {
    val viewModel: AutomationViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()

    when (state) {
        AutomationStep.LocationPermission -> LocationPermissionScreen()
    }
}