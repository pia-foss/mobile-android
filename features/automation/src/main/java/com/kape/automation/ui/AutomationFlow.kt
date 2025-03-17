package com.kape.automation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.kape.automation.ui.screens.AddNewRuleScreen
import com.kape.automation.ui.screens.AutomationScreen
import com.kape.automation.ui.screens.LocationPermissionScreen
import com.kape.automation.ui.viewmodel.AutomationViewModel
import com.kape.automation.utils.AutomationStep
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AutomationFlow() {
    val viewModel: AutomationViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()

    when (state) {
        AutomationStep.LocationPermission -> LocationPermissionScreen()
        AutomationStep.AddRule -> AddNewRuleScreen()
        AutomationStep.MainSet -> AutomationScreen(isSet = true)
        AutomationStep.MainUpdate -> AutomationScreen(isSet = false)
    }
}