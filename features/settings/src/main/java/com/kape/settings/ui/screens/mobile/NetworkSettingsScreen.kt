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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.data.CustomDns
import com.kape.settings.data.DnsOptions
import com.kape.settings.domain.usecase.ReconnectPrompt
import com.kape.settings.ui.elements.CustomDnsDialog
import com.kape.settings.ui.elements.DnsSelectionDialog
import com.kape.settings.ui.elements.ReconnectPromptDialog
import com.kape.settings.ui.elements.TextDialog
import com.kape.settings.ui.vm.NetworkSettingsViewModel
import com.kape.ui.R
import com.kape.ui.Screen
import com.kape.ui.mobile.elements.SettingsItem
import com.kape.ui.mobile.elements.SettingsToggle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NetworkSettingsScreen() =
    Screen {
        val viewModel: NetworkSettingsViewModel = koinViewModel()
        val customDns by viewModel.customDns.collectAsStateWithLifecycle()
        val appBarViewModel: AppBarViewModel =
            koinViewModel<AppBarViewModel>().apply {
                appBarText(stringResource(id = R.string.networks))
            }
        val dnsOptions =
            mutableMapOf(
                DnsOptions.PIA to stringResource(id = R.string.pia),
                DnsOptions.SYSTEM to stringResource(id = R.string.network_dns_selection_system),
            )
        if (customDns.isInUse()) {
            dnsOptions[DnsOptions.CUSTOM] =
                "${stringResource(id = R.string.network_dns_selection_custom)} ${
                    getCustomDnsInfo(customDns)
                }"
        }
        val selectedOption by viewModel.selectedDnsOption.collectAsStateWithLifecycle()
        val dnsDialogVisible = rememberSaveable { mutableStateOf(false) }
        val customDnsDialogVisible = rememberSaveable { mutableStateOf(false) }
        val allowLocalTrafficDialogVisible = rememberSaveable { mutableStateOf(false) }
        val dnsWarningDialogVisible = rememberSaveable { mutableStateOf(false) }
        val portForwardingEnabled by viewModel
            .isPortForwardingEnabled()
            .collectAsStateWithLifecycle()
        val localTrafficEnabled by viewModel
            .isAllowLocalTrafficEnabled
            .collectAsStateWithLifecycle()
        val maceEnabled by viewModel.maceEnabled.collectAsStateWithLifecycle()

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
                    SettingsItem(
                        titleId = R.string.network_dns_title,
                        subtitle = dnsOptions[selectedOption],
                    ) {
                        dnsDialogVisible.value = !dnsDialogVisible.value
                    }
                    SettingsToggle(
                        titleId = R.string.network_port_forwarding_title,
                        subtitleId = R.string.network_port_forwarding_description,
                        enabled = portForwardingEnabled,
                        toggle = {
                            viewModel.toggleEnablePortForwarding(it)
                            viewModel.reconnectPrompt.onSettingChanged()
                        },
                    )
                    SettingsToggle(
                        titleId = R.string.network_allow_lan_traffic_title,
                        subtitleId = R.string.network_allow_lan_traffic_description,
                        stateEnabled = localTrafficEnabled,
                        toggle = { checked ->
                            viewModel.toggleAllowLocalNetwork(checked)
                            viewModel.reconnectPrompt.onSettingChanged()
                        },
                    )
                }
            }
        }

        if (dnsDialogVisible.value) {
            DnsSelectionDialog(
                options = dnsOptions,
                selection = selectedOption,
                onConfirm = {
                    val hasDnsOptionChanged = selectedOption != it
                    val previousDnsSelectionWasPIA = selectedOption == DnsOptions.PIA
                    dnsDialogVisible.value = false
                    viewModel.setSelectedDnsOption(it)

                    if (it == DnsOptions.SYSTEM &&
                        localTrafficEnabled.not()
                    ) {
                        allowLocalTrafficDialogVisible.value = true
                    }

                    if (hasDnsOptionChanged) {
                        if (previousDnsSelectionWasPIA) {
                            dnsWarningDialogVisible.value = true
                        } else {
                            viewModel.reconnectPrompt.onSettingChanged()
                        }
                        if (it != DnsOptions.PIA) {
                            viewModel.toggleMace(false)
                        }
                    }
                },
                onDismiss = {
                    dnsDialogVisible.value = false
                },
                onEdit = {
                    customDnsDialogVisible.value = true
                    dnsDialogVisible.value = false
                },
            )
        }

        if (customDnsDialogVisible.value) {
            CustomDnsDialog(
                customDns = customDns,
                displayFootnote = maceEnabled,
                onConfirm = {
                    customDnsDialogVisible.value = false
                    val hasCustomDnsChanged = customDns != it
                    viewModel.setCustomDns(
                        customDns = it,
                    )
                    if (it.isInUse()) {
                        viewModel.setSelectedDnsOption(DnsOptions.CUSTOM)
                        if (hasCustomDnsChanged) {
                            viewModel.reconnectPrompt.onSettingChanged()
                        }
                    } else {
                        viewModel.setSelectedDnsOption(DnsOptions.PIA)
                        viewModel.reconnectPrompt.onSettingChanged()
                    }
                },
                onDismiss = { customDnsDialogVisible.value = false },
                isDnsNumeric = { viewModel.isNumericIpAddress(it) },
            )
        }

        ReconnectPromptDialog(viewModel.reconnectPrompt)

        if (allowLocalTrafficDialogVisible.value) {
            AllowLanDialog(
                titleId = R.string.network_dns_selection_system,
                descriptionId = R.string.network_dns_selection_system_lan_requirement,
                onAllowLocalNetwork = { viewModel.toggleAllowLocalNetwork(true) },
                reconnectPrompt = viewModel.reconnectPrompt,
                allowLocalTrafficDialogVisible = allowLocalTrafficDialogVisible,
                onDismiss = {
                    viewModel.setSelectedDnsOption(DnsOptions.PIA)
                },
            )
        }

        if (dnsWarningDialogVisible.value) {
            UnsafeDnsWarningDialog(
                viewModel = viewModel,
                dnsSelectedOption = selectedOption,
                dnsWarningDialogVisible = dnsWarningDialogVisible,
                allowLocalTrafficDialogVisible = allowLocalTrafficDialogVisible,
            )
        }
    }

@Composable
fun UnsafeDnsWarningDialog(
    viewModel: NetworkSettingsViewModel,
    dnsSelectedOption: DnsOptions,
    dnsWarningDialogVisible: MutableState<Boolean>,
    allowLocalTrafficDialogVisible: MutableState<Boolean>,
) {
    val titleId =
        if (dnsSelectedOption == DnsOptions.SYSTEM) {
            R.string.network_dns_selection_system
        } else {
            R.string.network_dns_selection_custom
        }
    TextDialog(
        titleId = titleId,
        descriptionId = R.string.network_dns_selection_unsafe_warning,
        onDismiss = {
            viewModel.setSelectedDnsOption(DnsOptions.PIA)
            dnsWarningDialogVisible.value = false
            allowLocalTrafficDialogVisible.value = false
            viewModel.reconnectPrompt.onLater()
        },
        onConfirm = {
            dnsWarningDialogVisible.value = false
            if (!allowLocalTrafficDialogVisible.value) {
                viewModel.reconnectPrompt.onSettingChanged()
            }
        },
    )
}

@Composable
fun AllowLanDialog(
    titleId: Int,
    descriptionId: Int,
    allowLocalTrafficDialogVisible: MutableState<Boolean>,
    onAllowLocalNetwork: () -> Unit,
    reconnectPrompt: ReconnectPrompt,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    TextDialog(
        titleId = titleId,
        descriptionId = descriptionId,
        onDismiss = {
            allowLocalTrafficDialogVisible.value = false
            reconnectPrompt.onLater()
            onDismiss()
        },
        onConfirm = {
            onAllowLocalNetwork()
            allowLocalTrafficDialogVisible.value = false
            reconnectPrompt.onSettingChanged()
            onConfirm()
        },
    )
}

fun getCustomDnsInfo(customDns: CustomDns): String =
    if (customDns.primaryDns.isEmpty()) {
        ""
    } else if (customDns.secondaryDns.isEmpty()) {
        "(${customDns.primaryDns})"
    } else {
        "(${customDns.primaryDns}/${customDns.secondaryDns})"
    }