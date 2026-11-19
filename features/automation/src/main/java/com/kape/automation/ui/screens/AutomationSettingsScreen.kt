package com.kape.automation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.view.mobile.AppBarType
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.automation.ui.vm.AutomationViewModel
import com.kape.ui.R
import com.kape.ui.Screen
import com.kape.ui.mobile.elements.SettingsItem
import com.kape.ui.mobile.elements.SettingsToggle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AutomationSettingsScreen() =
    Screen {
        val viewModel: AutomationViewModel = koinViewModel()
        val appBarViewModel: AppBarViewModel =
            koinViewModel<AppBarViewModel>().apply {
                appBarText(stringResource(id = R.string.automation))
            }
        val context = LocalContext.current
        val isEnabled by viewModel.isAutomationEnabled.collectAsStateWithLifecycle()

        Scaffold(
            topBar = {
                AppBar(
                    viewModel = appBarViewModel,
                    type = AppBarType.Navigation,
                )
            },
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(it)
                        .fillMaxWidth()
                        .semantics {
                            testTagsAsResourceId = true
                        },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(modifier = Modifier.widthIn(max = 520.dp)) {
                    SettingsToggle(
                        titleId = R.string.automation_title,
                        subtitleId = R.string.automation_description,
                        enabled = isEnabled,
                        toggle = {
                            viewModel.onAutomationToggled()
                        },
                        testTag = ":AutomationSettingsScreen:enable_toggle",
                    )
                    if (isEnabled) {
                        SettingsItem(
                            titleId = R.string.manage_automation,
                            onClick = {
                                viewModel.navigateToNextScreen()
                            },
                            testTag = ":AutomationSettingsScreen:manage_automation",
                        )
                    }
                }
            }
        }
    }