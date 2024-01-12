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
import com.kape.settings.ui.elements.DnsSelectionDialog
import com.kape.settings.ui.elements.InputFieldDialog
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
    val dnsOptions = mapOf(
        DnsOptions.PIA to stringResource(id = R.string.pia),
        DnsOptions.SYSTEM to stringResource(id = R.string.network_dns_selection_system),
        DnsOptions.CUSTOM to "${stringResource(id = R.string.network_dns_selection_custom)} ${
        getCustomDnsInfo(
            viewModel.getCustomDns(),
        )
        }",
    )
    val dnsSelection =
        remember { mutableStateOf(dnsOptions.getValue(viewModel.getSelectedDnsOption())) }
    val dnsDialogVisible = remember { mutableStateOf(false) }
    val customDnsDialogVisible = remember { mutableStateOf(false) }
    val allowLocalTrafficDialogVisible = remember { mutableStateOf(false) }

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
                },
            )
            SettingsToggle(
                titleId = R.string.network_allow_lan_traffic_title,
                subtitleId = R.string.network_allow_lan_traffic_description,
                stateEnabled = viewModel.isAllowLocalTrafficEnabled,
                toggle = { checked ->
                    viewModel.toggleAllowLocalNetwork(checked)
                },
            )
        }
    }

    if (dnsDialogVisible.value) {
        DnsSelectionDialog(
            options = dnsOptions,
            selection = viewModel.getSelectedDnsOption(),
            onConfirm = {
                dnsDialogVisible.value = false
                viewModel.setSelectedDnsOption(it)
                if (it == DnsOptions.SYSTEM &&
                    viewModel.isAllowLocalTrafficEnabled.value.not()
                ) {
                    allowLocalTrafficDialogVisible.value = true
                }
            },
            onDismiss = {
                dnsDialogVisible.value = false
            },
            onEdit = {
                viewModel.setSelectedDnsOption(DnsOptions.CUSTOM)
                customDnsDialogVisible.value = true
                dnsDialogVisible.value = false
            },
        )
    }

    if (customDnsDialogVisible.value) {
        CustomDnsDialog(
            viewModel = viewModel,
            dnsSelection = dnsSelection,
            customDnsDialogVisible = customDnsDialogVisible,
        )
    }

    if (allowLocalTrafficDialogVisible.value) {
        AllowLanDialog(
            viewModel = viewModel,
            dnsSelection = dnsSelection,
            allowLocalTrafficDialogVisible = allowLocalTrafficDialogVisible,
        )
    }
}

@Composable
fun CustomDnsDialog(
    viewModel: SettingsViewModel,
    dnsSelection: MutableState<String>,
    customDnsDialogVisible: MutableState<Boolean>,
) {
    val customPrimaryDns = remember { mutableStateOf(viewModel.getCustomDns().primaryDns) }
    val customSecondaryDns = remember { mutableStateOf(viewModel.getCustomDns().secondaryDns) }
    val footnote =
        if (viewModel.maceEnabled.value) stringResource(id = R.string.custom_dns_disabling_mace) else null
    InputFieldDialog(
        R.string.network_dns_selection_title,
        onClear = {
            customPrimaryDns.value = ""
            customSecondaryDns.value = ""
        },
        onConfirm = {
            val primaryDns = customPrimaryDns.value
            val secondaryDns = customSecondaryDns.value
            if (primaryDns.isEmpty() && secondaryDns.isEmpty()) {
                dnsSelection.value = DnsOptions.PIA.value
            } else if (
                (primaryDns.isNotEmpty() && viewModel.isDnsNumeric(ipAddress = primaryDns).not()) ||
                (
                    secondaryDns.isNotEmpty() && viewModel.isDnsNumeric(ipAddress = secondaryDns)
                        .not()
                    )
            ) {
                return@InputFieldDialog
            }

            customDnsDialogVisible.value = false
            viewModel.setCustomDns(
                customDns = CustomDns(
                    primaryDns = primaryDns,
                    secondaryDns = secondaryDns,
                ),
            )
        },
        footnote,
    ) {
        viewModel.isDnsNumeric(it)
    }
}

@Composable
fun AllowLanDialog(
    viewModel: SettingsViewModel,
    dnsSelection: MutableState<String>,
    allowLocalTrafficDialogVisible: MutableState<Boolean>,
) {
    TextDialog(
        titleId = R.string.network_dns_selection_system,
        descriptionId = R.string.network_dns_selection_system_lan_requirement,
        onDismiss = {
            dnsSelection.value = DnsOptions.PIA.value
            allowLocalTrafficDialogVisible.value = false
        },
        onConfirm = {
            viewModel.toggleAllowLocalNetwork(true)
            allowLocalTrafficDialogVisible.value = false
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