package com.kape.settings.ui.screens.tv

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.settings.data.DnsOptions
import com.kape.settings.ui.elements.CustomDnsDialog
import com.kape.settings.ui.elements.DnsSelectionDialog
import com.kape.settings.ui.elements.ReconnectDialog
import com.kape.settings.ui.elements.tv.TvSettingsItem
import com.kape.settings.ui.elements.tv.TvSettingsToggle
import com.kape.settings.ui.screens.mobile.AllowLanDialog
import com.kape.settings.ui.screens.mobile.UnsafeDnsWarningDialog
import com.kape.settings.ui.screens.mobile.getCustomDnsInfo
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.tv.text.AppBarTitleText
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionManager
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun TvNetworkSettingsScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val initialFocusRequester = FocusRequester()

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

    LaunchedEffect(key1 = Unit) {
        initialFocusRequester.requestFocus()
    }

    BackHandler {
        viewModel.navigateUp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 4.dp,
            color = getTopBarConnectionColor(
                status = connectionStatus.value,
                scheme = LocalColors.current,
            ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, top = 24.dp, end = 32.dp, bottom = 0.dp)
                .background(LocalColors.current.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppBarTitleText(
                    content = stringResource(id = R.string.networks),
                    textColor = LocalColors.current.onSurface,
                    isError = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(end = 64.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                ) {
                    TvSettingsItem(
                        modifier = Modifier.focusRequester(initialFocusRequester),
                        titleId = R.string.network_dns_title,
                        subtitle = dnsOptions[viewModel.getSelectedDnsOption()],
                    ) {
                        dnsDialogVisible.value = !dnsDialogVisible.value
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TvSettingsToggle(
                        titleId = R.string.network_port_forwarding_title,
                        subtitleId = R.string.network_port_forwarding_description,
                        enabled = viewModel.isPortForwardingEnabled(),
                        toggle = {
                            viewModel.toggleEnablePortForwarding(it)
                            viewModel.showReconnectDialogIfVpnConnected()
                        },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TvSettingsToggle(
                        titleId = R.string.network_allow_lan_traffic_title,
                        subtitleId = R.string.network_allow_lan_traffic_description,
                        stateEnabled = viewModel.isAllowLocalTrafficEnabled,
                        toggle = {
                            viewModel.toggleAllowLocalNetwork(it)
                            viewModel.showReconnectDialogIfVpnConnected()
                        },
                    )
                }
                Column(
                    modifier = Modifier.weight(1.0f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_tv_settings),
                        contentScale = ContentScale.Fit,
                        contentDescription = null,
                    )
                }
            }
        }

        if (dnsDialogVisible.value) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.6f),
            ) {
                DnsSelectionDialog(
                    options = dnsOptions,
                    selection = viewModel.getSelectedDnsOption(),
                    onConfirm = {
                        val hasDnsOptionChanged = viewModel.getSelectedDnsOption() != it
                        val previousDnsSelectionWasPIA =
                            viewModel.getSelectedDnsOption() == DnsOptions.PIA
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
        }

        if (customDnsDialogVisible.value) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.6f),
            ) {
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
        }

        if (viewModel.reconnectDialogVisible.value) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.6f),
            ) {
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
        }

        if (allowLocalTrafficDialogVisible.value) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.6f),
            ) {
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
        }

        if (dnsWarningDialogVisible.value) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.6f),
            ) {
                UnsafeDnsWarningDialog(
                    viewModel = viewModel,
                    dnsWarningDialogVisible = dnsWarningDialogVisible,
                    allowLocalTrafficDialogVisible = allowLocalTrafficDialogVisible,
                )
            }
        }
    }
}