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
import com.kape.settings.data.DataEncryption
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.settings.ui.elements.OptionsDialog
import com.kape.settings.ui.elements.SettingsItem
import com.kape.settings.ui.elements.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProtocolSettingsScreen() {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.protocols))
    }
    val protocolDialogVisible = remember { mutableStateOf(false) }
    val protocolSelection = remember { mutableStateOf(viewModel.getSelectedProtocol().name) }

    Scaffold(
        topBar = {
            AppBar(
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
                    R.string.protocol_selection_title,
                    options = listOf(
                        VpnProtocols.OpenVPN.name,
                        VpnProtocols.WireGuard.name,
                    ),
                    onDismiss = {
                        protocolDialogVisible.value = false
                    },
                    onConfirm = {
                        VpnProtocols.fromName(protocolSelection.value)?.let {
                            viewModel.selectProtocol(it)
                        }
                        protocolDialogVisible.value = false
                    },
                    selection = protocolSelection,
                )
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
    val transportSelection =
        remember { mutableStateOf(viewModel.getOpenVpnSettings().transport.value) }
    val encryptionDialogVisible = remember { mutableStateOf(false) }
    val encryptionSelection =
        remember { mutableStateOf(viewModel.getOpenVpnSettings().dataEncryption.value) }
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
            transportSelection = transportSelection,
            portSelection = portSelection,
        )
    }

    if (encryptionDialogVisible.value) {
        EncryptionSelectionDialog(
            viewModel = viewModel,
            encryptionDialogVisible = encryptionDialogVisible,
            encryptionSelection = encryptionSelection,
        )
    }

    if (portDialogVisible.value) {
        PortSelectionDialog(
            viewModel = viewModel,
            portDialogVisible = portDialogVisible,
            portSelection = portSelection,
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
    transportSelection: MutableState<String>,
    portSelection: MutableState<String>,
) {
    OptionsDialog(
        R.string.protocol_transport_title,
        options = listOf(
            Transport.UDP.value,
            Transport.TCP.value,
        ),
        onDismiss = {
            transportDialogVisible.value = false
        },
        onConfirm = {
            Transport.fromName(transportSelection.value)?.let {
                viewModel.setTransport(it)
                portSelection.value = viewModel.getOpenVpnSettings().port
            }
            transportDialogVisible.value = false
        },
        selection = transportSelection,
    )
}

@Composable
fun EncryptionSelectionDialog(
    viewModel: SettingsViewModel,
    encryptionDialogVisible: MutableState<Boolean>,
    encryptionSelection: MutableState<String>,
) {
    OptionsDialog(
        R.string.protocol_data_encryption_title,
        options = listOf(
            DataEncryption.AES_128_GCM.value,
            DataEncryption.AES_256_GCM.value,
        ),
        onDismiss = {
            encryptionDialogVisible.value = false
        },
        onConfirm = {
            DataEncryption.fromName(encryptionSelection.value)?.let {
                viewModel.setEncryption(it)
            }
            encryptionDialogVisible.value = false
        },
        selection = encryptionSelection,
    )
}

@Composable
fun PortSelectionDialog(
    viewModel: SettingsViewModel,
    portDialogVisible: MutableState<Boolean>,
    portSelection: MutableState<String>,
) {
    OptionsDialog(
        R.string.protocol_port_title,
        options = viewModel.getPorts(),
        onDismiss = {
            portDialogVisible.value = false
        },
        onConfirm = {
            viewModel.setPort(portSelection.value)
            portDialogVisible.value = false
        },
        selection = portSelection,
    )
}