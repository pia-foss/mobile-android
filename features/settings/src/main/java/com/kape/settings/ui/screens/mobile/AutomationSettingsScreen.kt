package com.kape.settings.ui.screens.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.view.mobile.AppBarType
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.automation.ui.viewmodel.AutomationViewModel
import com.kape.settings.ui.elements.mobile.SettingsItem
import com.kape.settings.ui.elements.mobile.SettingsToggle
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AutomationSettingsScreen() = Screen {
    val viewModel: AutomationViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.automation))
    }
    val state by viewModel.automationState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
                type = AppBarType.Navigation,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth()
                .semantics {},
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                SettingsToggle(
                    titleId = R.string.automation_title,
                    subtitleId = R.string.automation_description,
                    enabled = state.isEnabled,
                    toggle = {
                        viewModel.onAutomationToggled(context)
                    },
                )
                if (state.isEnabled) {
                    SettingsItem(
                        titleId = R.string.manage_automation,
                        onClick = {
                            viewModel.navigateToNextScreen()
                        },
                    )
                }
            }
        }
    }
}