package com.kape.settings.ui.screens.mobile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.ui.elements.mobile.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun GeneralSettingsScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.general))
    }

    BackHandler {
        viewModel.navigateUp()
    }

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
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
                    titleId = R.string.connect_on_boot_title,
                    subtitleId = R.string.connect_on_boot_description,
                    enabled = viewModel.launchOnBootEnabled.value,
                    toggle = {
                        viewModel.toggleLaunchOnBoot(it)
                    },
                )

                SettingsToggle(
                    titleId = R.string.connect_on_launch_title,
                    subtitleId = R.string.connect_on_launch_description,
                    enabled = viewModel.connectOnStart.value,
                    toggle = {
                        viewModel.toggleConnectOnStart(it)
                    },
                )

                SettingsToggle(
                    titleId = R.string.connect_on_update_title,
                    subtitleId = R.string.connect_on_update_description,
                    enabled = viewModel.connectOnUpdate.value,
                    toggle = {
                        viewModel.toggleConnectOnUpdate(it)
                    },
                )
            }
        }
    }
}