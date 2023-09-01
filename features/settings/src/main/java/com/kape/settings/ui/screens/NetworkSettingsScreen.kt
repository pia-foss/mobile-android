package com.kape.settings.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.NavigationAppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.ui.elements.SettingsItem
import com.kape.settings.ui.elements.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettingsScreen() {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.networks))
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
            SettingsItem(
                titleId = R.string.network_dns_title,
                subtitle = stringResource(id = R.string.network_dns_title),
            )
            Divider(
                color = LocalColors.current.outline,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            SettingsToggle(
                titleId = R.string.network_port_forwarding_title,
                subtitleId = R.string.network_port_forwarding_description,
                enabled = false, toggle = {},
            )
            Divider(
                color = LocalColors.current.outline,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            SettingsToggle(
                titleId = R.string.network_allow_lan_traffic_title,
                subtitleId = R.string.network_allow_lan_traffic_description,
                enabled = false, toggle = {},
            )
        }
    }
}