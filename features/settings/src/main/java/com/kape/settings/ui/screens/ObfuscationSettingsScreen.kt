package com.kape.settings.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kape.appbar.view.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.Transport
import com.kape.settings.ui.elements.CustomObfuscationDialog
import com.kape.settings.ui.elements.ObfuscationSelectionDialog
import com.kape.settings.ui.elements.ReconnectDialog
import com.kape.settings.ui.elements.SettingsItem
import com.kape.settings.ui.elements.SettingsToggle
import com.kape.settings.ui.elements.TextDialog
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
    val allowLocalTrafficDialogVisible = remember { mutableStateOf(false) }
    val tcpTransportDialogVisible = remember { mutableStateOf(false) }
    val externalProxyAppDialogVisible = remember { mutableStateOf(false) }

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
            SettingsToggle(
                titleId = R.string.obfuscation_external_proxy_app_title,
                subtitleId = R.string.obfuscation_external_proxy_app_description,
                stateEnabled = viewModel.externalProxyAppEnabled,
                toggle = {
                    if (it && viewModel.externalProxyAppPackageName.value.isEmpty()) {
                        externalProxyAppDialogVisible.value = true
                    } else {
                        viewModel.toggleExternalProxyApp(it)
                    }
                },
            )
            if (viewModel.externalProxyAppEnabled.value && viewModel.externalProxyAppPackageName.value.isNotEmpty()) {
                SettingsItem(
                    titleId = R.string.selected_proxy_app,
                    subtitle = viewModel.externalProxyAppPackageName.value,
                    onClick = {
                        viewModel.navigateToExternalAppList()
                    },
                )
                SettingsItem(
                    titleId = R.string.proxy_port,
                    subtitle = viewModel.externalProxyAppPort.value,
                    onClick = {

                    },
                )
            }
            SettingsToggle(
                titleId = R.string.obfuscation_shadowsocks_title,
                subtitleId = R.string.obfuscation_shadowsocks_description,
                stateEnabled = viewModel.shadowsocksObfuscationEnabled,
                toggle = { enabled ->
                    viewModel.toggleShadowsocksObfuscation(enabled)
                    if (viewModel.isAllowLocalTrafficEnabled.value.not()) {
                        allowLocalTrafficDialogVisible.value = true
                    } else if (viewModel.getTransport() != Transport.TCP) {
                        tcpTransportDialogVisible.value = true
                    }
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

    if (viewModel.reconnectDialogVisible.value) {
        ReconnectDialog(
            onReconnect = {
                viewModel.reconnect()
                viewModel.reconnectDialogVisible.value = false
            },
            onLater = {
                viewModel.reconnectDialogVisible.value = false
            },
        )
    }

    if (allowLocalTrafficDialogVisible.value) {
        AllowLanDialog(
            titleId = R.string.obfuscation_shadowsocks_title,
            descriptionId = R.string.obfuscation_shadowsocks_lan_requirement,
            viewModel = viewModel,
            allowLocalTrafficDialogVisible = allowLocalTrafficDialogVisible,
            onDismiss = {
                viewModel.toggleShadowsocksObfuscation(false)
            },
            onConfirm = {
                if (viewModel.getTransport() != Transport.TCP) {
                    tcpTransportDialogVisible.value = true
                }
            },
        )
    }

    if (tcpTransportDialogVisible.value) {
        UpdateOpenVPNTransportToTcpDialog(
            titleId = R.string.obfuscation_shadowsocks_title,
            descriptionId = R.string.obfuscation_shadowsocks_tcp_requirement,
            viewModel = viewModel,
            tcpTransportDialogVisible = tcpTransportDialogVisible,
            onDismiss = {
                viewModel.toggleShadowsocksObfuscation(false)
            },
        )
    }

    if (externalProxyAppDialogVisible.value) {
        ExternalProxyAppDialog(
            titleId = R.string.enable_proxy_dialog_title,
            descriptionId = R.string.enable_proxy_dialog_message,
            onConfirm = {
                externalProxyAppDialogVisible.value = false
                viewModel.navigateToExternalAppList()
            },
            onDismiss = {
                externalProxyAppDialogVisible.value = false
                viewModel.toggleExternalProxyApp(false)
            },
        )
    }
}

@Composable
fun UpdateOpenVPNTransportToTcpDialog(
    titleId: Int,
    descriptionId: Int,
    viewModel: SettingsViewModel,
    tcpTransportDialogVisible: MutableState<Boolean>,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    TextDialog(
        titleId = titleId,
        descriptionId = descriptionId,
        onDismiss = {
            tcpTransportDialogVisible.value = false
            viewModel.setTransport(Transport.UDP)
            onDismiss()
        },
        onConfirm = {
            viewModel.setTransport(Transport.TCP)
            tcpTransportDialogVisible.value = false
            onConfirm()
        },
    )
}

@Composable
fun ExternalProxyAppDialog(
    titleId: Int,
    descriptionId: Int,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    TextDialog(
        titleId = titleId,
        descriptionId = descriptionId,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}