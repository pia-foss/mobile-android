package com.kape.settings.ui.screens

import androidx.activity.compose.BackHandler
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
import com.kape.settings.data.CustomDns
import com.kape.settings.data.DnsOptions
import com.kape.settings.ui.elements.CustomDnsDialog
import com.kape.settings.ui.elements.DnsSelectionDialog
import com.kape.settings.ui.elements.ReconnectDialog
import com.kape.settings.ui.elements.SettingsItem
import com.kape.settings.ui.elements.SettingsToggle
import com.kape.settings.ui.elements.TextDialog
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.elements.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun NetworkSettingsScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.networks))
    }
    val dnsOptions = mutableMapOf(
        DnsOptions.PIA to stringResource(id = R.string.pia),
        DnsOptions.SYSTEM to stringResource(id = R.string.network_dns_selection_system),
    )
    if (viewModel.getCustomDns().isInUse()) {
        dnsOptions[DnsOptions.CUSTOM] =
            "${stringResource(id = R.string.network_dns_selection_custom)} ${
            getCustomDnsInfo(
                viewModel.getCustomDns(),
            )
            }"
    }

    val dnsSelection =
        remember { mutableStateOf(dnsOptions.getValue(viewModel.getSelectedDnsOption())) }
    val dnsDialogVisible = remember { mutableStateOf(false) }
    val customDnsDialogVisible = remember { mutableStateOf(false) }
    val allowLocalTrafficDialogVisible = remember { mutableStateOf(false) }
    val dnsWarningDialogVisible = remember { mutableStateOf(false) }

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
                .padding(it),
        ) {
            SettingsItem(
                titleId = R.string.network_dns_title,
                subtitle = dnsOptions[viewModel.getSelectedDnsOption()],
            ) {
                dnsDialogVisible.value = !dnsDialogVisible.value
            }
            SettingsToggle(
                titleId = R.string.network_port_forwarding_title,
                subtitleId = R.string.network_port_forwarding_description,
                enabled = viewModel.isPortForwardingEnabled(),
                toggle = {
                    viewModel.toggleEnablePortForwarding(it)
                    viewModel.showReconnectDialogIfVpnConnected()
                },
            )
            SettingsToggle(
                titleId = R.string.network_allow_lan_traffic_title,
                subtitleId = R.string.network_allow_lan_traffic_description,
                stateEnabled = viewModel.isAllowLocalTrafficEnabled,
                toggle = { checked ->
                    viewModel.toggleAllowLocalNetwork(checked)
                    viewModel.showReconnectDialogIfVpnConnected()
                },
            )
        }
    }

    if (dnsDialogVisible.value) {
        DnsSelectionDialog(
            options = dnsOptions,
            selection = viewModel.getSelectedDnsOption(),
            onConfirm = {
                val hasDnsOptionChanged = viewModel.getSelectedDnsOption() != it
                val previousDnsSelectionWasPIA = viewModel.getSelectedDnsOption() == DnsOptions.PIA
                dnsDialogVisible.value = false
                viewModel.setSelectedDnsOption(it)

                if (it == DnsOptions.SYSTEM &&
                    viewModel.isAllowLocalTrafficEnabled.value.not()
                ) {
                    allowLocalTrafficDialogVisible.value = true
                }

                if (hasDnsOptionChanged) {
                    // Only show the warning dialog if the user was on a safe DNS option (PIA DNS)
                    if (previousDnsSelectionWasPIA) {
                        dnsWarningDialogVisible.value = true
                    } else {
                        viewModel.showReconnectDialogIfVpnConnected()
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
            customDns = viewModel.getCustomDns(),
            displayFootnote = viewModel.maceEnabled.value,
            onConfirm = {
                customDnsDialogVisible.value = false
                val hasCustomDnsChanged = viewModel.getCustomDns() != it
                viewModel.setCustomDns(
                    customDns = it,
                )
                if (it.isInUse()) {
                    viewModel.setSelectedDnsOption(DnsOptions.CUSTOM)
                    if (hasCustomDnsChanged) {
                        viewModel.showReconnectDialogIfVpnConnected()
                    }
                } else {
                    viewModel.setSelectedDnsOption(DnsOptions.PIA)
                    viewModel.showReconnectDialogIfVpnConnected()
                }
            },
            onDismiss = { customDnsDialogVisible.value = false },
            isDnsNumeric = { viewModel.isNumericIpAddress(it) },
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
            titleId = R.string.network_dns_selection_system,
            descriptionId = R.string.network_dns_selection_system_lan_requirement,
            viewModel = viewModel,
            allowLocalTrafficDialogVisible = allowLocalTrafficDialogVisible,
            onDismiss = {
                dnsSelection.value = DnsOptions.PIA.value
                viewModel.setSelectedDnsOption(DnsOptions.PIA)
            },
        )
    }

    if (dnsWarningDialogVisible.value) {
        UnsafeDnsWarningDialog(
            viewModel = viewModel,
            dnsWarningDialogVisible = dnsWarningDialogVisible,
            allowLocalTrafficDialogVisible = allowLocalTrafficDialogVisible,
        )
    }
}

@Composable
fun UnsafeDnsWarningDialog(
    viewModel: SettingsViewModel,
    dnsWarningDialogVisible: MutableState<Boolean>,
    allowLocalTrafficDialogVisible: MutableState<Boolean>,
) {
    val titleId = if (viewModel.getSelectedDnsOption() == DnsOptions.SYSTEM) {
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
            viewModel.reconnectDialogVisible.value = false
        },
        onConfirm = {
            dnsWarningDialogVisible.value = false
            if (!allowLocalTrafficDialogVisible.value) {
                viewModel.showReconnectDialogIfVpnConnected()
            }
        },
    )
}

@Composable
fun AllowLanDialog(
    titleId: Int,
    descriptionId: Int,
    viewModel: SettingsViewModel,
    allowLocalTrafficDialogVisible: MutableState<Boolean>,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    TextDialog(
        titleId = titleId,
        descriptionId = descriptionId,
        onDismiss = {
            allowLocalTrafficDialogVisible.value = false
            viewModel.reconnectDialogVisible.value = false
            onDismiss()
        },
        onConfirm = {
            viewModel.toggleAllowLocalNetwork(true)
            allowLocalTrafficDialogVisible.value = false
            viewModel.showReconnectDialogIfVpnConnected()
            onConfirm()
        },
    )
}

private fun getCustomDnsInfo(customDns: CustomDns): String {
    return if (customDns.primaryDns.isEmpty()) {
        ""
    } else if (customDns.secondaryDns.isEmpty()) {
        "(${customDns.primaryDns})"
    } else {
        "(${customDns.primaryDns}/${customDns.secondaryDns})"
    }
}