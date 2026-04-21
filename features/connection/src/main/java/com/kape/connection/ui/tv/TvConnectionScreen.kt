package com.kape.connection.ui.tv

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.kape.appbar.view.tv.TvHomeHeaderItem
import com.kape.connection.ui.ConnectButton
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.contracts.ConnectionInfoProvider
import com.kape.customization.data.Element
import com.kape.data.ConnectionStatus
import com.kape.data.vpnserver.VpnServer
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.tv.tiles.QuickConnect
import com.kape.ui.tv.tiles.VpnLocationPicker
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.Locale

@Composable
fun TvConnectionScreen() =
    Screen {
        val viewModel: ConnectionViewModel = koinViewModel()
        val connectionInfoProvider: ConnectionInfoProvider = koinInject()
        val state by connectionInfoProvider.connectionInfoState.collectAsState()
        val isConnected = viewModel.isConnected.collectAsState()
        val topStartHeaderFocusRequester = remember { FocusRequester() }
        val topEndHeaderFocusRequester = remember { FocusRequester() }
        val startQuickConnectFocusRequester = remember { FocusRequester() }
        val locale = Locale.getDefault().language
        val lifecycleOwner = LocalLifecycleOwner.current
        val activity = LocalActivity.current

        BackHandler {
            activity?.finish()
        }

        LaunchedEffect(lifecycleOwner) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.refreshState()
            }
        }

        LaunchedEffect(key1 = Unit) {
            topStartHeaderFocusRequester.requestFocus()
            viewModel.autoConnect()
        }

        Box(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
        ) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 4.dp,
                color =
                    connectionInfoProvider.getTopBarConnectionColor(
                        scheme = LocalColors.current,
                    ),
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 24.dp)
                        .background(LocalColors.current.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                TvHomeHeaderItem(
                    connectionStatus = state.status,
                    defaultSelectedTabIndex = 0,
                    topStartHeaderFocusRequester = topStartHeaderFocusRequester,
                    topEndHeaderFocusRequester = topEndHeaderFocusRequester,
                    onLocationsSelected = {
                        viewModel.showVpnRegionSelection()
                    },
                    onSettingsSelected = {
                        viewModel.navigateToSideMenu()
                    },
                    onHelpSelected = {
                        viewModel.navigateToHelp()
                    },
                )
                Column(
                    modifier =
                        Modifier
                            .widthIn(max = 520.dp)
                            .fillMaxHeight(),
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    ConnectButton(
                        status = if (isConnected.value) state.status else ConnectionStatus.ERROR,
                        onTvLayout = true,
                        modifier =
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .focusProperties {
                                    start = topStartHeaderFocusRequester
                                    end = topEndHeaderFocusRequester
                                },
                    ) {
                        viewModel.onConnectionButtonClicked()
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    viewModel.getOrderedElements().forEach {
                        DisplayComponent(
                            screenElement = it.element,
                            isVisible = viewModel.isScreenElementVisible(it),
                            viewModel = viewModel,
                            startQuickConnectFocusRequester = startQuickConnectFocusRequester,
                        )
                    }
                }
            }
        }
    }

@Composable
private fun DisplayComponent(
    screenElement: Element,
    isVisible: Boolean,
    viewModel: ConnectionViewModel,
    startQuickConnectFocusRequester: FocusRequester,
) {
    if (isVisible.not()) {
        return
    }

    val state by viewModel.state.collectAsState()
    val connectionState by viewModel.connectionInfoProvider.connectionInfoState.collectAsState()
    val vpnState by viewModel.connectionInfoProvider.state.collectAsState()

    when (screenElement) {
        Element.VpnRegionSelection -> {
            VpnLocationPicker(
                modifier =
                    Modifier.focusProperties {
                        down = startQuickConnectFocusRequester
                    },
                server = state.server,
                vpnIp = vpnState.vpnIp,
                isConnected = connectionState.status == ConnectionStatus.CONNECTED,
                isOptimal = state.isCurrentServerOptimal,
            ) {
                viewModel.showVpnRegionSelection()
            }
        }

        Element.QuickConnect -> {
            val quickConnectMap = mutableMapOf<VpnServer?, Boolean>()
            for (server in state.quickConnectServers) {
                quickConnectMap[server] = viewModel.isVpnServerFavorite(server.name, server.isDedicatedIp)
            }
            QuickConnect(
                startQuickConnectFocusRequester = startQuickConnectFocusRequester,
                servers = quickConnectMap,
                onClick = {
                    viewModel.quickConnect(it)
                },
            )
        }

        Element.ShadowsocksRegionSelection,
        Element.ConnectionInfo,
        Element.IpInfo,
        Element.QuickSettings,
        Element.Snooze,
        Element.Traffic,
        -> {
            // Continue. Not showing them on TV.
        }
    }
}