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
import com.kape.appbar.view.NavigationAppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.data.CustomDns
import com.kape.settings.data.DnsOptions
import com.kape.settings.ui.elements.InputFieldDialog
import com.kape.settings.ui.elements.OptionsDialog
import com.kape.settings.ui.elements.SettingsItem
import com.kape.settings.ui.elements.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.elements.InputFieldProperties
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettingsScreen() {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.networks))
    }
    val dnsOptions = mapOf(
        DnsOptions.PIA to stringResource(id = R.string.pia),
        DnsOptions.CUSTOM to stringResource(id = R.string.network_dns_selection_custom),
    )
    val dnsSelection =
        remember { mutableStateOf(dnsOptions.getValue(viewModel.getSelectedDnsOption())) }
    val dnsDialogVisible = remember { mutableStateOf(false) }
    val customDnsDialogVisible = remember { mutableStateOf(false) }

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
                enabled = viewModel.isAllowLocalTrafficEnabled(),
                toggle = {
                    viewModel.toggleAllowLocalNetwork(it)
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
        )
    }

    if (customDnsDialogVisible.value) {
        CustomDnsDialog(
            viewModel = viewModel,
            customDnsDialogVisible = customDnsDialogVisible,
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
) {
    OptionsDialog(
        R.string.network_dns_selection_title,
        options = listOf(
            dnsOptions.getValue(DnsOptions.PIA),
            dnsOptions.getValue(DnsOptions.CUSTOM),
        ),
        onDismiss = {
            dnsDialogVisible.value = false
        },
        onConfirm = {
            dnsDialogVisible.value = false
        },
        onOptionSelected = {
            when (it) {
                dnsOptions[DnsOptions.PIA] -> {
                    viewModel.setSelectedDnsOption(DnsOptions.PIA)
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
    customDnsDialogVisible: MutableState<Boolean>,
) {
    val customDnsPrimary = remember { mutableStateOf(viewModel.getCustomDns().primaryDns) }
    val customDnsSecondary = remember { mutableStateOf(viewModel.getCustomDns().secondaryDns) }
    InputFieldDialog(
        R.string.network_dns_selection_title,
        inputFieldProperties = listOf(
            InputFieldProperties(
                stringResource(id = R.string.network_dns_selection_custom_primary),
                maskInput = false,
                keyboardType = KeyboardType.Decimal,
                content = customDnsPrimary,
            ),
            InputFieldProperties(
                stringResource(id = R.string.network_dns_selection_custom_secondary),
                maskInput = false,
                keyboardType = KeyboardType.Decimal,
                content = customDnsSecondary,
            ),
        ),
        onClear = {
            customDnsPrimary.value = ""
            customDnsSecondary.value = ""
        },
        onConfirm = {
            customDnsDialogVisible.value = false
            viewModel.setCustomDns(
                customDns = CustomDns(
                    primaryDns = customDnsPrimary.value,
                    secondaryDns = customDnsSecondary.value,
                ),
            )
        },
    )
}