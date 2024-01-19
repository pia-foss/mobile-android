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
import com.kape.settings.ui.elements.SettingsItem
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.elements.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun ObfuscationSettingsScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.obfuscation))
    }
    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
                onLeftIconClick = { viewModel.navigateUp() },
            ) {
                viewModel.exitObfuscationSettings()
            }
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it),
        ) {
            SettingsItem(
                titleId = R.string.obfuscation_external_proxy_app_title,
                subtitle = stringResource(id = R.string.obfuscation_external_proxy_app_description),
                onClick = {
                    TODO("To be implemented")
                },
            )
            SettingsItem(
                titleId = R.string.obfuscation_shadowsocks_title,
                subtitle = stringResource(id = R.string.obfuscation_shadowsocks_description),
                onClick = {
                    TODO("To be implemented")
                },
            )
        }
    }
}