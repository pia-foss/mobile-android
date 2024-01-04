package com.kape.customization

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.AppBar
import com.kape.appbar.view.AppBarType
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.customization.data.Element
import com.kape.customization.data.ScreenElement
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.ui.R
import com.kape.ui.elements.Screen
import com.kape.ui.elements.Visibility
import com.kape.ui.tiles.ConnectionInfo
import com.kape.ui.tiles.IPTile
import com.kape.ui.tiles.LocationPicker
import com.kape.ui.tiles.QuickConnect
import com.kape.ui.tiles.QuickSettings
import com.kape.ui.tiles.Snooze
import com.kape.ui.tiles.Traffic
import com.kape.ui.utils.LocalColors
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun CustomizationScreen() = Screen {
    val viewModel: CustomizationViewModel = koinViewModel()
    val connectionViewModel: ConnectionViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel()
    val locale = Locale.getDefault().language

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
                type = AppBarType.InAppBrowser,
                onLeftIconClick = { viewModel.exitCustomization() },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it),
        ) {
            LaunchedEffect(key1 = Unit) {
                connectionViewModel.loadVpnServers(locale)
            }
            val state = rememberReorderableLazyListState(onMove = viewModel::onMove)
            LazyColumn(state = state.listState, modifier = Modifier.reorderable(state)) {
                items(viewModel.getOrderedElements()) { item ->
                    ReorderableItem(
                        state,
                        viewModel.getOrderedElements().indexOf(item),
                    ) { dragging ->
                        Box(
                            modifier = Modifier
                                .detectReorderAfterLongPress(state)
                                .fillMaxWidth(),
                        ) {
                            CreateCustomizableElement(
                                item,
                                connectionViewModel,
                                viewModel::toggleVisibility,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCustomizableElement(
    screenElement: ScreenElement,
    connectionViewModel: ConnectionViewModel,
    onVisibilityToggled: (element: Element, isVisible: Boolean) -> Unit,
) {
    val visible = remember { mutableStateOf(screenElement.isVisible) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Visibility(
            isChecked = visible.value,
            modifier = Modifier.clickable {
                visible.value = !visible.value
                onVisibilityToggled(screenElement.element, !screenElement.isVisible)
            },
        )
        Spacer(modifier = Modifier.width(16.dp))
        DisplayComponent(
            screenElement = screenElement,
            viewModel = connectionViewModel,
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_drag),
            contentDescription = null,
            tint = LocalColors.current.onSurface,
        )
    }
}

@Composable
private fun DisplayComponent(
    screenElement: ScreenElement,
    viewModel: ConnectionViewModel,
) {
    when (screenElement.element) {
        Element.ConnectionInfo -> {
            val settings = viewModel.getConnectionSettings()
            ConnectionInfo(
                connection = settings.name,
                port = settings.port,
                auth = settings.auth,
                transport = settings.transport.value,
                encryption = settings.dataEncryption.value,
                handshake = settings.handshake,
            )
        }

        Element.IpInfo -> {
            val state = viewModel.portForwardingStatus.collectAsState()
            IPTile(
                isPortForwardingEnabled = viewModel.isPortForwardingEnabled(),
                publicIp = viewModel.ip,
                vpnIp = viewModel.vpnIp,
                portForwardingStatus = when (state.value) {
                    PortForwardingStatus.Error -> stringResource(id = R.string.pfwd_error)
                    PortForwardingStatus.NoPortForwarding -> stringResource(id = R.string.pfwd_disabled)
                    PortForwardingStatus.Requesting -> stringResource(id = R.string.pfwd_requesting)
                    PortForwardingStatus.Success -> viewModel.port.value.toString()
                    else -> ""
                },
            )
        }

        Element.QuickConnect -> {
            QuickConnect(
                servers = viewModel.quickConnectVpnServers.value,
                onClick = {},
            )
        }

        Element.QuickSettings -> {
            QuickSettings(
                onKillSwitchClick = {},
                onAutomationClick = {},
                onProtocolsClick = {},
            )
        }

        Element.RegionSelection -> {
            viewModel.selectedVpnServer.value?.let {
                LocationPicker(server = it, isConnected = viewModel.isConnectionActive()) {}
            }
        }

        Element.Snooze -> {
            Snooze(
                viewModel.snoozeActive,
                onClick = {},
                onResumeClick = {},
            )
        }

        Element.Traffic -> {
            Traffic(
                viewModel.download.value,
                viewModel.upload.value,
            )
        }
    }
}