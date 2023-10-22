package com.kape.settings.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.IAppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.data.CustomDns
import com.kape.settings.data.DnsOptions
import com.kape.settings.ui.elements.InputFieldDialog
import com.kape.settings.ui.elements.OptionsDialog
import com.kape.settings.ui.elements.SettingsItem
import com.kape.settings.ui.elements.SettingsToggle
import com.kape.settings.ui.elements.TextDialog
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.elements.InputFieldProperties
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun NetworkSettingsScreen() {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.networks))
    }
    val dnsOptions = mapOf(
        DnsOptions.PIA to stringResource(id = R.string.pia),
        DnsOptions.SYSTEM to stringResource(id = R.string.network_dns_selection_system),
        DnsOptions.CUSTOM to stringResource(id = R.string.network_dns_selection_custom),
    )
    val dnsSelection =
        remember { mutableStateOf(dnsOptions.getValue(viewModel.getSelectedDnsOption())) }
    val dnsDialogVisible = remember { mutableStateOf(false) }
    val customDnsDialogVisible = remember { mutableStateOf(false) }
    val allowLocalTrafficDialogVisible = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            IAppBar(
                viewModel = appBarViewModel,
            ) {
                viewModel.navigateUp()
            }
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it),
        ) {
            SettingsItem(
                titleId = R.string.network_dns_title,
                subtitle = dnsSelection.value,
            ) {
                dnsDialogVisible.value = !dnsDialogVisible.value
            }
            Divider(
                color = LocalColors.current.outline,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            SettingsToggle(
                titleId = R.string.network_port_forwarding_title,
                subtitleId = R.string.network_port_forwarding_description,
                enabled = viewModel.isPortForwardingEnabled(),
                toggle = {
                    viewModel.toggleEnablePortForwarding(it)
                },
            )
            Divider(
                color = LocalColors.current.outline,
                modifier = Modifier.padding(vertical = 8.dp),
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
            viewModel = viewModel,
            dnsSelection = dnsSelection,
            dnsOptions = dnsOptions,
            dnsDialogVisible = dnsDialogVisible,
            customDnsDialogVisible = customDnsDialogVisible,
            allowLocalTrafficDialogVisible = allowLocalTrafficDialogVisible,
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
fun DnsSelectionDialog(
    viewModel: SettingsViewModel,
    dnsSelection: MutableState<String>,
    dnsOptions: Map<DnsOptions, String>,
    dnsDialogVisible: MutableState<Boolean>,
    customDnsDialogVisible: MutableState<Boolean>,
    allowLocalTrafficDialogVisible: MutableState<Boolean>,
) {
    OptionsDialog(
        R.string.network_dns_selection_title,
        options = listOf(
            dnsOptions.getValue(DnsOptions.PIA),
            dnsOptions.getValue(DnsOptions.SYSTEM),
            dnsOptions.getValue(DnsOptions.CUSTOM),
        ),
        onDismiss = { },
        onConfirm = {
            dnsDialogVisible.value = false
            if (viewModel.getSelectedDnsOption() == DnsOptions.SYSTEM &&
                viewModel.isAllowLocalTrafficEnabled.value.not()
            ) {
                allowLocalTrafficDialogVisible.value = true
            }
        },
        onOptionSelected = {
            when (it) {
                dnsOptions[DnsOptions.PIA] -> {
                    viewModel.setSelectedDnsOption(DnsOptions.PIA)
                }

                dnsOptions[DnsOptions.SYSTEM] -> {
                    viewModel.setSelectedDnsOption(DnsOptions.SYSTEM)
                }

                dnsOptions[DnsOptions.CUSTOM] -> {
                    viewModel.setSelectedDnsOption(DnsOptions.CUSTOM)
                    customDnsDialogVisible.value = true
                    dnsDialogVisible.value = false
                }
            }
        },
        selection = dnsSelection,
    )
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
        inputFieldProperties = listOf(
            InputFieldProperties(
                stringResource(id = R.string.network_dns_selection_custom_primary),
                maskInput = false,
                keyboardType = KeyboardType.Decimal,
                content = customPrimaryDns,
                error = if (
                    customPrimaryDns.value.isEmpty() ||
                    viewModel.isDnsNumeric(ipAddress = customPrimaryDns.value)
                ) {
                    null
                } else {
                    stringResource(id = R.string.network_dns_selection_custom_invalid)
                },
            ),
            InputFieldProperties(
                stringResource(id = R.string.network_dns_selection_custom_secondary),
                maskInput = false,
                keyboardType = KeyboardType.Decimal,
                content = customSecondaryDns,
                error = if (
                    customSecondaryDns.value.isEmpty() ||
                    viewModel.isDnsNumeric(ipAddress = customSecondaryDns.value)
                ) {
                    null
                } else {
                    stringResource(id = R.string.network_dns_selection_custom_invalid)
                },
            ),
        ),
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
                (secondaryDns.isNotEmpty() && viewModel.isDnsNumeric(ipAddress = secondaryDns)
                    .not())
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
    )
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