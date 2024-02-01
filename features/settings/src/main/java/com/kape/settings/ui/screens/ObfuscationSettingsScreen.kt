package com.kape.settings.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kape.appbar.view.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.ui.elements.CustomObfuscationDialog
import com.kape.settings.ui.elements.ObfuscationSelectionDialog
import com.kape.settings.ui.elements.SettingsItem
import com.kape.settings.ui.elements.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.elements.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun ObfuscationSettingsScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.obfuscation))
    }
    val obfuscationOptions = mutableMapOf(
        ObfuscationOptions.PIA to stringResource(id = R.string.pia),
    )
    viewModel.getCustomObfuscation()?.let {
        obfuscationOptions[ObfuscationOptions.CUSTOM] =
            "${stringResource(id = R.string.network_dns_selection_custom)} ${it.host}:${it.port}"
    }

    val customObfuscationDialogVisible = remember { mutableStateOf(false) }
    val obfuscationDialogVisible = remember { mutableStateOf(false) }

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
            SettingsToggle(
                titleId = R.string.obfuscation_shadowsocks_title,
                subtitleId = R.string.obfuscation_shadowsocks_description,
                enabled = viewModel.shadowsocksObfuscationEnabled.value,
                toggle = { enabled ->
                    viewModel.toggleShadowsocksObfuscation(enabled)
                },
            )
            if (viewModel.shadowsocksObfuscationEnabled.value) {
                SettingsItem(
                    titleId = R.string.obfuscation_shadowsocks_subtitle,
                    onClick = {
                        obfuscationDialogVisible.value = !obfuscationDialogVisible.value
                    },
                )
            }
        }
    }

    if (obfuscationDialogVisible.value) {
        ObfuscationSelectionDialog(
            options = obfuscationOptions,
            selection = viewModel.getSelectedObfuscationOption(),
            onConfirm = {
                obfuscationDialogVisible.value = false
                val hasCustomOptionChanged = viewModel.getSelectedObfuscationOption() != it
                viewModel.setSelectedObfuscationOption(it)

                if (hasCustomOptionChanged &&
                    (viewModel.shadowsocksObfuscationEnabled.value && it == ObfuscationOptions.PIA)
                ) {
                    viewModel.showReconnectDialogIfVpnConnected()
                }
            },
            onDismiss = {
                obfuscationDialogVisible.value = false
            },
            onEdit = {
                customObfuscationDialogVisible.value = true
                obfuscationDialogVisible.value = false
            },
        )
    }

    if (customObfuscationDialogVisible.value) {
        CustomObfuscationDialog(
            customObfuscation = viewModel.getCustomObfuscation(),
            onConfirm = {
                customObfuscationDialogVisible.value = false
                val hasCustomObfuscationChanged = viewModel.getCustomObfuscation() != it
                viewModel.setCustomObfuscation(customObfuscation = it)
                viewModel.setSelectedObfuscationOption(ObfuscationOptions.CUSTOM)

                if (hasCustomObfuscationChanged) {
                    viewModel.showReconnectDialogIfVpnConnected()
                }
            },
            onDismiss = {
                customObfuscationDialogVisible.value = false
            },
            isNumericIpAddress = {
                viewModel.isNumericIpAddress(it)
            },
            isPortValid = {
                viewModel.isPortValid(it)
            },
        )
    }
}