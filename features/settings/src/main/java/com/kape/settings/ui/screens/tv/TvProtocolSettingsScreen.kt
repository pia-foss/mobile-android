package com.kape.settings.ui.screens.tv

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import com.kape.settings.data.VpnProtocols
import com.kape.settings.ui.elements.OptionsDialog
import com.kape.settings.ui.elements.ReconnectDialog
import com.kape.settings.ui.elements.tv.TvSettingsItem
import com.kape.settings.ui.elements.tv.TvSettingsToggle
import com.kape.settings.ui.screens.mobile.EncryptionSelectionDialog
import com.kape.settings.ui.screens.mobile.PortSelectionDialog
import com.kape.settings.ui.screens.mobile.TransportSelectionDialog
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.settings.utils.getDefaultButtons
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.tv.text.AppBarTitleText
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionManager
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun TvProtocolSettingsScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val initialFocusRequester = FocusRequester()

    val protocolDialogVisible = remember { mutableStateOf(false) }
    val transportDialogVisible = remember { mutableStateOf(false) }
    val encryptionDialogVisible = remember { mutableStateOf(false) }
    val portDialogVisible = remember { mutableStateOf(false) }
    val protocolSelection = remember { mutableStateOf(viewModel.getSelectedProtocol()) }
    val portSelection = remember { mutableStateOf(viewModel.getOpenVpnSettings().port) }

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
                    content = stringResource(id = R.string.protocols),
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
                    when (viewModel.getSelectedProtocol()) {
                        VpnProtocols.WireGuard ->
                            TvWireguardProtocolSettingsScreen(
                                viewModel = viewModel,
                                initialFocusRequester = initialFocusRequester,
                                protocolDialogVisible = protocolDialogVisible,
                            )
                        VpnProtocols.OpenVPN ->
                            TvOpenVpnProtocolSettingsScreen(
                                viewModel = viewModel,
                                initialFocusRequester = initialFocusRequester,
                                protocolDialogVisible = protocolDialogVisible,
                                transportDialogVisible = transportDialogVisible,
                                encryptionDialogVisible = encryptionDialogVisible,
                                portDialogVisible = portDialogVisible,
                            )
                    }
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
        if (protocolDialogVisible.value) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.6f),
            ) {
                OptionsDialog(
                    titleId = R.string.protocol_selection_title,
                    options = mapOf(
                        VpnProtocols.OpenVPN to VpnProtocols.OpenVPN.name,
                        VpnProtocols.WireGuard to VpnProtocols.WireGuard.name,
                    ),
                    buttons = getDefaultButtons(),
                    onDismiss = { protocolDialogVisible.value = false },
                    onConfirm = {
                        val hasProtocolChanged = protocolSelection.value != it
                        viewModel.selectProtocol(it)
                        protocolSelection.value = it
                        protocolDialogVisible.value = false

                        if (hasProtocolChanged) {
                            viewModel.showReconnectDialogIfVpnConnected()
                        }
                    },
                    selection = protocolSelection.value,
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

        if (transportDialogVisible.value) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.6f),
            ) {
                TransportSelectionDialog(
                    viewModel = viewModel,
                    transportDialogVisible = transportDialogVisible,
                    transportSelection = viewModel.getOpenVpnSettings().transport,
                    portSelection = portSelection,
                )
            }
        }

        if (encryptionDialogVisible.value) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.6f),
            ) {
                EncryptionSelectionDialog(
                    viewModel = viewModel,
                    encryptionDialogVisible = encryptionDialogVisible,
                    encryptionSelection = viewModel.getOpenVpnSettings().dataEncryption,
                )
            }
        }

        if (portDialogVisible.value) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.6f),
            ) {
                PortSelectionDialog(
                    viewModel = viewModel,
                    portDialogVisible = portDialogVisible,
                    portSelection = viewModel.getOpenVpnSettings().port,
                )
            }
        }
    }
}

@Composable
private fun TvOpenVpnProtocolSettingsScreen(
    viewModel: SettingsViewModel,
    initialFocusRequester: FocusRequester,
    protocolDialogVisible: MutableState<Boolean>,
    transportDialogVisible: MutableState<Boolean>,
    encryptionDialogVisible: MutableState<Boolean>,
    portDialogVisible: MutableState<Boolean>,
) {
    TvSettingsItem(
        modifier = Modifier.focusRequester(initialFocusRequester),
        titleId = R.string.protocol_selection_title,
        subtitle = viewModel.getOpenVpnSettings().name,
    ) {
        protocolDialogVisible.value = true
    }
    TvSettingsItem(
        titleId = R.string.protocol_transport_title,
        subtitle = viewModel.getOpenVpnSettings().transport.value,
    ) {
        transportDialogVisible.value = !transportDialogVisible.value
    }
    TvSettingsItem(
        titleId = R.string.protocol_data_encryption_title,
        subtitle = viewModel.getOpenVpnSettings().dataEncryption.value,
    ) {
        encryptionDialogVisible.value = !encryptionDialogVisible.value
    }
    TvSettingsItem(
        titleId = R.string.protocol_port_title,
        subtitle = viewModel.getOpenVpnSettings().port,
    ) {
        portDialogVisible.value = !portDialogVisible.value
    }
    TvSettingsToggle(
        titleId = R.string.protocol_use_small_packets_title,
        subtitleId = R.string.protocol_use_small_packets_description,
        enabled = viewModel.getOpenVpnSettings().useSmallPackets,
        toggle = {
            viewModel.setOpenVpnEnableSmallPackets(it)
        },
    )
    TvSettingsItem(
        titleId = R.string.protocol_handshake_title,
        subtitle = viewModel.getOpenVpnSettings().handshake,
    ) { }
}

@Composable
private fun TvWireguardProtocolSettingsScreen(
    viewModel: SettingsViewModel,
    initialFocusRequester: FocusRequester,
    protocolDialogVisible: MutableState<Boolean>,
) {
    TvSettingsItem(
        modifier = Modifier.focusRequester(initialFocusRequester),
        titleId = R.string.protocol_selection_title,
        subtitle = viewModel.getWireGuardSettings().name,
    ) {
        protocolDialogVisible.value = true
    }
    TvSettingsToggle(
        titleId = R.string.protocol_use_small_packets_title,
        subtitleId = R.string.protocol_use_small_packets_description,
        enabled = viewModel.getWireGuardSettings().useSmallPackets,
        toggle = {
            viewModel.setWireGuardEnableSmallPackets(it)
        },
    )
    TvSettingsItem(
        titleId = R.string.protocol_handshake_title,
        subtitle = viewModel.getWireGuardSettings().handshake,
    ) { }
}