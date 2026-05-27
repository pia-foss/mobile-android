package com.kape.settings.ui.screens.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.ui.elements.mobile.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun GeneralSettingsScreen() =
    Screen {
        val viewModel: SettingsViewModel = koinViewModel()
        val appBarViewModel: AppBarViewModel =
            koinViewModel<AppBarViewModel>().apply {
                appBarText(stringResource(id = R.string.general))
            }
        val launchOnBoot by viewModel.launchOnBootEnabled.collectAsStateWithLifecycle()
        val connectOnStart by viewModel.connectOnStart.collectAsStateWithLifecycle()
        val connectOnUpdate by viewModel.connectOnUpdate.collectAsStateWithLifecycle()
        val showGeoLocatedServers by viewModel.showGeoLocatedServers.collectAsStateWithLifecycle()

        Scaffold(
            topBar = {
                AppBar(viewModel = appBarViewModel)
            },
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(it)
                        .fillMaxWidth()
                        .semantics {},
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(modifier = Modifier.widthIn(max = 520.dp)) {
                    SettingsToggle(
                        titleId = R.string.connect_on_boot_title,
                        subtitleId = R.string.connect_on_boot_description,
                        enabled = launchOnBoot,
                        toggle = {
                            viewModel.toggleLaunchOnBoot(it)
                        },
                    )

                    SettingsToggle(
                        titleId = R.string.connect_on_launch_title,
                        subtitleId = R.string.connect_on_launch_description,
                        enabled = connectOnStart,
                        toggle = {
                            viewModel.toggleConnectOnStart(it)
                        },
                    )

                    SettingsToggle(
                        titleId = R.string.connect_on_update_title,
                        subtitleId = R.string.connect_on_update_description,
                        enabled = connectOnUpdate,
                        toggle = {
                            viewModel.toggleConnectOnUpdate(it)
                        },
                    )

                    SettingsToggle(
                        titleId = R.string.geo_servers,
                        enabled = showGeoLocatedServers,
                        toggle = {
                            viewModel.toggleShowGeoLocatedServers(it)
                        },
                    )
                }
            }
        }
    }