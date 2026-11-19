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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.appbar.view.topBarColor
import com.kape.appbar.view.tv.TvHomeHeaderItem
import com.kape.connection.ui.ConnectButton
import com.kape.connection.ui.vm.ConnectionTilesViewModel
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.customization.data.Element
import com.kape.data.ConnectionStatus
import com.kape.ui.Screen
import com.kape.ui.tv.tiles.QuickConnect
import com.kape.ui.tv.tiles.VpnLocationPicker
import com.kape.ui.utils.LocalColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TvConnectionScreen(
    viewModel: ConnectionViewModel = koinViewModel(),
    tiles: ConnectionTilesViewModel = koinViewModel(),
) = Screen {
    val connectionStatus by viewModel.status.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val elements by viewModel.elements.collectAsStateWithLifecycle()
    val hasUpdateAvailable by viewModel.hasUpdateAvailable.collectAsStateWithLifecycle()
    val topStartHeaderFocusRequester = remember { FocusRequester() }
    val topEndHeaderFocusRequester = remember { FocusRequester() }
    val startQuickConnectFocusRequester = remember { FocusRequester() }
    val activity = LocalActivity.current

    BackHandler {
        activity?.finish()
    }

    LaunchedEffect(Unit) {
        topStartHeaderFocusRequester.requestFocus()
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
            color = connectionStatus.topBarColor(LocalColors.current),
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
                connectionStatus = connectionStatus,
                defaultSelectedTabIndex = 0,
                hasUpdateAvailable = hasUpdateAvailable,
                topStartHeaderFocusRequester = topStartHeaderFocusRequester,
                topEndHeaderFocusRequester = topEndHeaderFocusRequester,
                onLocationsSelected = viewModel::onRegionClick,
                onSettingsSelected = viewModel::navigateToSideMenu,
                onHelpSelected = viewModel::navigateToHelp,
            )
            Column(
                modifier =
                    Modifier
                        .widthIn(max = 520.dp)
                        .fillMaxHeight(),
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                ConnectButton(
                    status = if (isOnline) connectionStatus else ConnectionStatus.ERROR,
                    onTvLayout = true,
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .focusProperties {
                                start = topStartHeaderFocusRequester
                                end = topEndHeaderFocusRequester
                            },
                    onClick = viewModel::onConnectionButtonClicked,
                )
                Spacer(modifier = Modifier.height(32.dp))
                elements.filter { it.shouldDisplayElement }.forEach {
                    when (it.element) {
                        Element.VpnRegionSelection -> {
                            val displayed by tiles.displayedServer.collectAsStateWithLifecycle()
                            val vpnIp by tiles.vpnIp.collectAsStateWithLifecycle()
                            displayed?.let { shown ->
                                VpnLocationPicker(
                                    modifier = Modifier.focusProperties { down = startQuickConnectFocusRequester },
                                    server = shown.server,
                                    vpnIp = vpnIp,
                                    isConnected = connectionStatus == ConnectionStatus.CONNECTED,
                                    isOptimal = shown.isOptimal,
                                    onClick = viewModel::onRegionClick,
                                )
                            }
                        }

                        Element.QuickConnect -> {
                            val servers by tiles.quickConnect.collectAsStateWithLifecycle()
                            QuickConnect(
                                startQuickConnectFocusRequester = startQuickConnectFocusRequester,
                                servers = servers.associate { item -> item.server to item.isFavorite },
                                onClick = viewModel::onQuickConnect,
                            )
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
}