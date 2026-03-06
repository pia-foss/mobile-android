package com.kape.automation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.kape.automation.ui.screens.AddNewRuleScreen
import com.kape.automation.ui.screens.AutomationScreen
import com.kape.automation.ui.screens.BackgroundLocationPermissionScreen
import com.kape.automation.ui.screens.LocationPermissionScreen
import com.kape.automation.ui.viewmodel.AutomationViewModel
import com.kape.automation.utils.AutomationStep
import com.kape.router.AutomationAddRule
import com.kape.router.AutomationBackgroundLocation
import com.kape.router.AutomationLocation
import com.kape.router.AutomationSet
import com.kape.router.AutomationUpdate
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AutomationFlow() {
    val viewModel: AutomationViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()

    when (state) {
        AutomationStep.LocationPermission -> viewModel.router.updateDestination(AutomationLocation)
        AutomationStep.EnableBackgroundLocation -> viewModel.router.updateDestination(
            AutomationBackgroundLocation,
        )

        AutomationStep.AddRule -> viewModel.router.updateDestination(AutomationAddRule)
        AutomationStep.MainSet -> viewModel.router.updateDestination(AutomationSet)
        AutomationStep.MainUpdate -> viewModel.router.updateDestination(AutomationUpdate)
    }
}