package com.kape.settings.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kape.appbar.view.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.ui.elements.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.elements.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun GeneralSettingsScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.general))
    }

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
            ) {
                viewModel.navigateUp()
            }
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it),
        ) {
            SettingsToggle(
                titleId = R.string.connect_on_boot_title,
                subtitleId = R.string.connect_on_boot_description,
                enabled = viewModel.launchOnBootEnabled,
                toggle = {
                    viewModel.toggleLaunchOnBoot(it)
                },
            )

            SettingsToggle(
                titleId = R.string.connect_on_launch_title,
                subtitleId = R.string.connect_on_launch_description,
                enabled = viewModel.connectOnStart,
                toggle = {
                    viewModel.toggleConnectOnStart(it)
                },
            )

            SettingsToggle(
                titleId = R.string.connect_on_update_title,
                subtitleId = R.string.connect_on_update_title,
                enabled = viewModel.connectOnUpdate,
                toggle = {
                    viewModel.toggleConnectOnUpdate(it)
                },
            )
        }
    }
}