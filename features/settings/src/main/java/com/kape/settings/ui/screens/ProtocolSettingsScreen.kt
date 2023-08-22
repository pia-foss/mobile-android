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
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.NavigationAppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.ui.elements.OptionsDialog
import com.kape.settings.ui.elements.SettingsItem
import com.kape.settings.ui.elements.SettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.settings.utils.VpnProtocols
import com.kape.settings.utils.DataEncryption
import com.kape.settings.utils.Transport
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
        remember { mutableStateOf(viewModel.getOpenVpnSettings().transport.name) }
    val encryptionDialogVisible = remember { mutableStateOf(false) }
    val encryptionSelection =
        remember { mutableStateOf(viewModel.getOpenVpnSettings().dataEncryption.name) }
    val portDialogVisible = remember { mutableStateOf(false) }
    val portSelection = remember { mutableStateOf(viewModel.getOpenVpnSettings().port) }
    ProtocolSelectionLine(
        name = viewModel.getOpenVpnSettings().name,
        protocolDialogVisible,
    )
    Divider(
        color = LocalColors.current.outline,
        modifier = Modifier.padding(vertical = 8.dp),
    )
    SettingsItem(
        titleId = R.string.protocol_transport_title,
        subtitle = viewModel.getOpenVpnSettings().transport.name,
    ) {
        transportDialogVisible.value = !transportDialogVisible.value
    }
    Divider(
        color = LocalColors.current.outline,
        modifier = Modifier.padding(vertical = 8.dp),
    )
    SettingsItem(
        titleId = R.string.protocol_data_encryption_title,
        subtitle = viewModel.getOpenVpnSettings().dataEncryption.name,
    ) {
        encryptionDialogVisible.value = !encryptionDialogVisible.value
    }
    Divider(
        color = LocalColors.current.outline,
        modifier = Modifier.padding(vertical = 8.dp),
    )
    SettingsItem(
        titleId = R.string.protocol_port_title,
        subtitle = viewModel.getOpenVpnSettings().port,
    ) {
        portDialogVisible.value = !portDialogVisible.value
    }
    Divider(
        color = LocalColors.current.outline,
        modifier = Modifier.padding(vertical = 8.dp),
    )
    UseSmallPacketsLine(
        enabled = viewModel.getOpenVpnSettings().useSmallPackets,
        onClick = {
            viewModel.setOpenVpnEnableSmallPackets(it)
        },
    )
    Divider(
        color = LocalColors.current.outline,
        modifier = Modifier.padding(vertical = 8.dp),
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
    Divider(
        color = LocalColors.current.outline,
        modifier = Modifier.padding(vertical = 8.dp),
    )
    UseSmallPacketsLine(
        enabled = protocolSettings.useSmallPackets,
        onClick = {
            viewModel.setWireGuardEnableSmallPackets(it)
        },
    )
    Divider(
        color = LocalColors.current.outline,
        modifier = Modifier.padding(vertical = 8.dp),
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
            Transport.UDP.name,
            Transport.TCP.name,
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
            DataEncryption.AES_128_GCM.name,
            DataEncryption.AES_256_GCM.name,
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