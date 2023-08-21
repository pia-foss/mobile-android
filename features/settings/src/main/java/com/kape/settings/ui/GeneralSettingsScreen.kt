package com.kape.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kape.appbar.view.NavigationAppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen() {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.general))
    }

    Scaffold(
        topBar = {
            NavigationAppBar(
                viewModel = appBarViewModel,
                onLeftButtonClick = {
                    viewModel.navigateUp()
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
        ) {
            SettingsToggle(
                titleId = R.string.connect_on_boot_title,
                subtitleId = R.string.connect_on_boot_description,
                enabled = viewModel.launchOnBootEnabled,
                toggle = {
                    viewModel.toggleLaunchOnBoot(it)
                }
            )
            Divider(color = LocalColors.current.outline)
            SettingsToggle(
                titleId = R.string.connect_on_launch_title,
                subtitleId = R.string.connect_on_launch_description,
                enabled = viewModel.connectOnStart,
                toggle = {
                    viewModel.toggleConnectOnStart(it)
                }
            )
            Divider(color = LocalColors.current.outline)
            SettingsToggle(
                titleId = R.string.connect_on_update_title,
                subtitleId = R.string.connect_on_update_title,
                enabled = viewModel.connectOnUpdate,
                toggle = {
                    viewModel.toggleConnectOnUpdate(it)
                }
            )
            Divider(color = LocalColors.current.outline)
        }
    }
}