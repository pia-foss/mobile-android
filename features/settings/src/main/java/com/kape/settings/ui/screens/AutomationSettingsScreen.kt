package com.kape.settings.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kape.appbar.view.NavigationAppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.ui.elements.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationSettingsScreen() {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.automation))
    }
    Scaffold(
        topBar = {
            NavigationAppBar(
                viewModel = appBarViewModel,
                onLeftButtonClick = {
                    viewModel.navigateUp()
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it),
        ) {
            SettingsToggle(
                titleId = R.string.automation_title,
                subtitleId = R.string.automation_description,
                enabled = false, toggle = {},
            )
        }
    }
}