package com.kape.settings.ui.screens.mobile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.view.mobile.AppBarType
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.ui.elements.mobile.SettingsItem
import com.kape.settings.ui.elements.mobile.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AutomationSettingsScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.automation))
    }
    val automationEnabled = remember { mutableStateOf(viewModel.isAutomationEnabled()) }

    BackHandler {
        viewModel.navigateUp()
    }

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
                type = AppBarType.Navigation,
                onLeftIconClick = { viewModel.navigateUp() },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                SettingsToggle(
                    titleId = R.string.automation_title,
                    subtitleId = R.string.automation_description,
                    enabled = viewModel.isAutomationEnabled(),
                    toggle = {
                        automationEnabled.value = it
                        if (viewModel.isAutomationEnabled()) {
                            viewModel.disableAutomation()
                        } else if (!viewModel.areLocationPermissionsGranted()) {
                            viewModel.navigateToAutomation()
                        }
                    },
                )
                if (automationEnabled.value) {
                    SettingsItem(
                        titleId = R.string.manage_automation,
                        onClick = {
                            viewModel.navigateToAutomation()
                        },
                    )
                }
            }
        }
    }
}