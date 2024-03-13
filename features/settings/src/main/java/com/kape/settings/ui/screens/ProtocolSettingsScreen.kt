package com.kape.settings.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.data.DataEncryption
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.settings.ui.elements.OptionsDialog
import com.kape.settings.ui.elements.ReconnectDialog
import com.kape.settings.ui.elements.SettingsItem
import com.kape.settings.ui.elements.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.settings.utils.getDefaultButtons
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProtocolSettingsScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.protocols))
    }
    val protocolDialogVisible = remember { mutableStateOf(false) }
    val protocolSelection = remember { mutableStateOf(viewModel.getSelectedProtocol()) }

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
                .padding(it)
                .fillMaxWidth()
                .semantics {
                    testTagsAsResourceId = true
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                when (viewModel.getSelectedProtocol()) {
                    VpnProtocols.OpenVPN -> {
                        OpenVpnProtocolSettingsScreen(
                            viewModel = viewModel,
                            protocolDialogVisible = protocolDialogVisible,
                        )
                    }

                    VpnProtocols.WireGuard -> {
                        WireGuardProtocolSettingsScreen(
                            viewModel = viewModel,
                            protocolDialogVisible = protocolDialogVisible,
                        )
                    }
                }

                if (protocolDialogVisible.value) {
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
            }
        }
    }
}

@Composable
fun OpenVpnProtocolSettingsScreen(
    viewModel: SettingsViewModel,
    protocolDialogVisible: MutableState<Boolean>,
) {
    val transportDialogVisible = remember { mutableStateOf(false) }
    val encryptionDialogVisible = remember { mutableStateOf(false) }
    val portDialogVisible = remember { mutableStateOf(false) }
    val portSelection = remember { mutableStateOf(viewModel.getOpenVpnSettings().port) }
    ProtocolSelectionLine(
        name = viewModel.getOpenVpnSettings().name,
        protocolDialogVisible,
    )
    SettingsItem(
        titleId = R.string.protocol_transport_title,
        subtitle = viewModel.getOpenVpnSettings().transport.value,
    ) {
        transportDialogVisible.value = !transportDialogVisible.value
    }
    SettingsItem(
        titleId = R.string.protocol_data_encryption_title,
        subtitle = viewModel.getOpenVpnSettings().dataEncryption.value,
    ) {
        encryptionDialogVisible.value = !encryptionDialogVisible.value
    }
    SettingsItem(
        titleId = R.string.protocol_port_title,
        subtitle = viewModel.getOpenVpnSettings().port,
    ) {
        portDialogVisible.value = !portDialogVisible.value
    }
    UseSmallPacketsLine(
        enabled = viewModel.getOpenVpnSettings().useSmallPackets,
        onClick = {
            viewModel.setOpenVpnEnableSmallPackets(it)
        },
    )
    HandshakeLine(handshake = viewModel.getOpenVpnSettings().handshake)

    if (transportDialogVisible.value) {
        TransportSelectionDialog(
            viewModel = viewModel,
            transportDialogVisible = transportDialogVisible,
            transportSelection = viewModel.getOpenVpnSettings().transport,
            portSelection = portSelection,
        )
    }

    if (encryptionDialogVisible.value) {
        EncryptionSelectionDialog(
            viewModel = viewModel,
            encryptionDialogVisible = encryptionDialogVisible,
            encryptionSelection = viewModel.getOpenVpnSettings().dataEncryption,
        )
    }

    if (portDialogVisible.value) {
        PortSelectionDialog(
            viewModel = viewModel,
            portDialogVisible = portDialogVisible,
            portSelection = viewModel.getOpenVpnSettings().port,
        )
    }
}

@Composable
fun WireGuardProtocolSettingsScreen(
    viewModel: SettingsViewModel,
    protocolDialogVisible: MutableState<Boolean>,
) {
    val protocolSettings = viewModel.getWireGuardSettings()
    ProtocolSelectionLine(name = protocolSettings.name, protocolDialogVisible)
    UseSmallPacketsLine(
        enabled = protocolSettings.useSmallPackets,
        onClick = {
            viewModel.setWireGuardEnableSmallPackets(it)
        },
    )
    HandshakeLine(handshake = protocolSettings.handshake)
}

@Composable
fun ProtocolSelectionLine(name: String, visibility: MutableState<Boolean>) {
    SettingsItem(
        titleId = R.string.protocol_selection_title,
        subtitle = name,
        testTag = ":ProtocolSettingsScreen:protocol_selection",
    ) {
        visibility.value = true
    }
}

@Composable
fun UseSmallPacketsLine(enabled: Boolean, onClick: (enabled: Boolean) -> Unit) {
    SettingsToggle(
        titleId = R.string.protocol_use_small_packets_title,
        subtitleId = R.string.protocol_use_small_packets_description,
        enabled = enabled,
        toggle = onClick,
    )
}

@Composable
fun HandshakeLine(handshake: String) {
    SettingsItem(
        titleId = R.string.protocol_handshake_title,
        subtitle = handshake,
    )
}

@Composable
fun TransportSelectionDialog(
    viewModel: SettingsViewModel,
    transportDialogVisible: MutableState<Boolean>,
    transportSelection: Transport,
    portSelection: MutableState<String>,
) {
    OptionsDialog(
        R.string.protocol_transport_title,
        options = mapOf(Transport.UDP to Transport.UDP.value, Transport.TCP to Transport.TCP.value),
        buttons = getDefaultButtons(),
        onDismiss = {
            transportDialogVisible.value = false
        },
        onConfirm = {
            viewModel.setTransport(it)
            portSelection.value = viewModel.getOpenVpnSettings().port
            transportDialogVisible.value = false
        },
        selection = transportSelection,
    )
}

@Composable
fun EncryptionSelectionDialog(
    viewModel: SettingsViewModel,
    encryptionDialogVisible: MutableState<Boolean>,
    encryptionSelection: DataEncryption,
) {
    OptionsDialog(
        R.string.protocol_data_encryption_title,
        options = mapOf(
            DataEncryption.AES_128_GCM to DataEncryption.AES_128_GCM.value,
            DataEncryption.AES_256_GCM to DataEncryption.AES_256_GCM.value,
        ),
        buttons = getDefaultButtons(),
        onDismiss = {
            encryptionDialogVisible.value = false
        },
        onConfirm = {
            viewModel.setEncryption(it)
            encryptionDialogVisible.value = false
        },
        selection = encryptionSelection,
    )
}

@Composable
fun PortSelectionDialog(
    viewModel: SettingsViewModel,
    portDialogVisible: MutableState<Boolean>,
    portSelection: String,
) {
    OptionsDialog(
        R.string.protocol_port_title,
        options = viewModel.getPorts(),
        buttons = getDefaultButtons(),
        onDismiss = {
            portDialogVisible.value = false
        },
        onConfirm = {
            viewModel.setPort(it.toString())
            portDialogVisible.value = false
        },
        selection = portSelection.toInt(),
    )
}