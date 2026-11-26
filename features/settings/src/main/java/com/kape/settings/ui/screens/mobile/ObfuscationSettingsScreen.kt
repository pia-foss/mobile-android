package com.kape.settings.ui.screens.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.Transport
import com.kape.settings.ui.elements.CustomObfuscationDialog
import com.kape.settings.ui.elements.ObfuscationSelectionDialog
import com.kape.settings.ui.elements.ProxyPortDialog
import com.kape.settings.ui.elements.ReconnectPromptDialog
import com.kape.settings.ui.elements.TextDialog
import com.kape.settings.ui.vm.ObfuscationSettingsViewModel
import com.kape.ui.R
import com.kape.ui.Screen
import com.kape.ui.mobile.elements.SettingsItem
import com.kape.ui.mobile.elements.SettingsToggle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ObfuscationSettingsScreen() =
    Screen {
        val viewModel: ObfuscationSettingsViewModel = koinViewModel()
        val customObfuscation by viewModel.customObfuscation.collectAsStateWithLifecycle()
        val appBarViewModel: AppBarViewModel =
            koinViewModel<AppBarViewModel>().apply {
                appBarText(stringResource(id = R.string.obfuscation))
            }
        val obfuscationOptions =
            mutableMapOf(
                ObfuscationOptions.PIA to stringResource(id = R.string.pia),
            )
        customObfuscation?.let {
            obfuscationOptions[ObfuscationOptions.CUSTOM] =
                "${stringResource(id = R.string.network_dns_selection_custom)} ${it.host}:${it.port}"
        }

        val customObfuscationDialogVisible = rememberSaveable { mutableStateOf(false) }
        val obfuscationDialogVisible = rememberSaveable { mutableStateOf(false) }
        val allowLocalTrafficDialogVisible = rememberSaveable { mutableStateOf(false) }
        val tcpTransportDialogVisible = rememberSaveable { mutableStateOf(false) }
        val externalProxyAppDialogVisible = rememberSaveable { mutableStateOf(false) }
        val externalProxyPortDialogVisible = rememberSaveable { mutableStateOf(false) }
        val externalProxyEnabled by viewModel.externalProxyAppEnabled.collectAsStateWithLifecycle()
        val externalProxyAppPackageName by viewModel.externalProxyAppPackageName.collectAsStateWithLifecycle()
        val externalProxyPort by viewModel.externalProxyAppPort.collectAsStateWithLifecycle()
        val shadowSocksEnabled by viewModel.shadowsocksObfuscationEnabled.collectAsStateWithLifecycle()
        val localTrafficEnabled by viewModel.isAllowLocalTrafficEnabled.collectAsStateWithLifecycle()
        val transport by viewModel.transport.collectAsStateWithLifecycle()
        val selectedObfuscationOption by viewModel.selectedObfuscationOption.collectAsStateWithLifecycle()
        val showExternalProxyTcpPrompt by viewModel.showExternalProxyTcpPrompt.collectAsStateWithLifecycle()

        Scaffold(
            topBar = {
                AppBar(viewModel = appBarViewModel, onRightIconClick = appBarViewModel::navigateBack)
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
                        titleId = R.string.obfuscation_external_proxy_app_title,
                        subtitleId = R.string.obfuscation_external_proxy_app_description,
                        stateEnabled = externalProxyEnabled,
                        toggle = {
                            if (it && externalProxyAppPackageName.isEmpty()) {
                                externalProxyAppDialogVisible.value = true
                            } else {
                                viewModel.toggleExternalProxyApp(it)
                            }
                        },
                    )
                    if (externalProxyEnabled && externalProxyAppPackageName.isNotEmpty()) {
                        SettingsItem(
                            titleId = R.string.selected_proxy_app,
                            subtitle = externalProxyAppPackageName,
                            onClick = {
                                viewModel.navigateToExternalAppList()
                            },
                        )
                        SettingsItem(
                            titleId = R.string.proxy_port,
                            subtitle = externalProxyPort,
                            onClick = {
                                externalProxyPortDialogVisible.value = true
                            },
                        )
                    }
                    SettingsToggle(
                        titleId = R.string.obfuscation_shadowsocks_title,
                        subtitleId = R.string.obfuscation_shadowsocks_description,
                        stateEnabled = shadowSocksEnabled,
                        toggle = { enabled ->
                            viewModel.toggleShadowsocksObfuscation(enabled)
                            if (localTrafficEnabled.not()) {
                                allowLocalTrafficDialogVisible.value = true
                            } else if (transport != Transport.TCP) {
                                tcpTransportDialogVisible.value = true
                            }
                        },
                    )
                    if (shadowSocksEnabled) {
                        SettingsItem(
                            titleId = R.string.obfuscation_shadowsocks_subtitle,
                            onClick = {
                                obfuscationDialogVisible.value = !obfuscationDialogVisible.value
                            },
                        )
                    }
                }
            }
        }

        if (obfuscationDialogVisible.value) {
            ObfuscationSelectionDialog(
                options = obfuscationOptions,
                selection = selectedObfuscationOption,
                onConfirm = {
                    obfuscationDialogVisible.value = false
                    val hasCustomOptionChanged = selectedObfuscationOption != it
                    viewModel.setSelectedObfuscationOption(it)

                    if (hasCustomOptionChanged &&
                        (shadowSocksEnabled && it == ObfuscationOptions.PIA)
                    ) {
                        viewModel.reconnectPrompt.onSettingChanged()
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
                customObfuscation = customObfuscation,
                onConfirm = {
                    customObfuscationDialogVisible.value = false
                    val hasCustomObfuscationChanged = customObfuscation != it
                    viewModel.setCustomObfuscation(customObfuscation = it)
                    viewModel.setSelectedObfuscationOption(ObfuscationOptions.CUSTOM)

                    if (hasCustomObfuscationChanged) {
                        viewModel.reconnectPrompt.onSettingChanged()
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

        ReconnectPromptDialog(viewModel.reconnectPrompt)

        if (allowLocalTrafficDialogVisible.value) {
            AllowLanDialog(
                titleId = R.string.obfuscation_shadowsocks_title,
                descriptionId = R.string.obfuscation_shadowsocks_lan_requirement,
                onAllowLocalNetwork = { viewModel.toggleAllowLocalNetwork(true) },
                reconnectPrompt = viewModel.reconnectPrompt,
                allowLocalTrafficDialogVisible = allowLocalTrafficDialogVisible,
                onDismiss = {
                    viewModel.toggleShadowsocksObfuscation(false)
                },
                onConfirm = {
                    if (transport != Transport.TCP) {
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

        if (externalProxyPortDialogVisible.value) {
            ProxyPortDialog(
                currentPort = externalProxyPort,
                onConfirm = {
                    viewModel.setExternalProxyPort(it)
                    externalProxyPortDialogVisible.value = false
                    viewModel.reconnectPrompt.onSettingChanged()
                },
                onDefault = {
                    viewModel.setExternalProxyPort(null)
                    externalProxyPortDialogVisible.value = false
                    viewModel.reconnectPrompt.onSettingChanged()
                },
                onDismiss = {
                    externalProxyPortDialogVisible.value = false
                },
            )
        }

        if (showExternalProxyTcpPrompt) {
            UpdateOpenVPNTransportToTcpDialog(
                titleId = R.string.external_proxy_problem_title,
                descriptionId = R.string.external_proxy_problem_message,
                viewModel = viewModel,
                tcpTransportDialogVisible = remember { mutableStateOf(true) },
                onDismiss = {
                    viewModel.toggleExternalProxyApp(false)
                },
            )
        }
    }

@Composable
fun UpdateOpenVPNTransportToTcpDialog(
    titleId: Int,
    descriptionId: Int,
    viewModel: ObfuscationSettingsViewModel,
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