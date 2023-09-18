package com.kape.connection.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.kape.appbar.view.ConnectionAppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.connection.ui.tiles.ConnectionInfoTile
import com.kape.connection.ui.tiles.FavoritesTile
import com.kape.connection.ui.tiles.IpInformationTile
import com.kape.connection.ui.tiles.QuickConnectTile
import com.kape.connection.ui.tiles.QuickSettingsTile
import com.kape.connection.ui.tiles.RegionInformationTile
import com.kape.connection.ui.tiles.SnoozeTile
import com.kape.connection.ui.tiles.UsageTile
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.connection.utils.SnoozeInterval
import com.kape.sidemenu.ui.SideMenuUiDrawer
import com.kape.ui.elements.Separator
import com.kape.ui.theme.Space
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.Locale

@Composable
fun ConnectionScreen() {
    val viewModel: ConnectionViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel()
    val locale = Locale.getDefault().language
    val context = LocalContext.current
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val connectionState = when (connectionStatus.value) {
        ConnectionStatus.CONNECTED -> ConnectionState.Connected
        ConnectionStatus.CONNECTING -> ConnectionState.Connecting
        ConnectionStatus.DISCONNECTED -> ConnectionState.Default
        ConnectionStatus.RECONNECTING -> ConnectionState.Connecting
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.loadServers(locale)
        viewModel.autoConnect()
    }

    SideMenuUiDrawer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            ConnectionAppBar(
                viewModel = appBarViewModel,
                onHeaderClick = { /*TODO*/ },
                onLeftButtonClick = { openDrawer() },
            )
            Spacer(modifier = Modifier.height(Space.NORMAL))
            ConnectionButton(connectionState) {
                viewModel.onConnectionButtonClicked()
            }
            viewModel.selectedServer.value?.let {
                RegionInformationTile(server = it) {
                    viewModel.showRegionSelection()
                }
            }
            Separator()
            IpInformationTile(
                ip = viewModel.ip,
                vpnIp = viewModel.vpnIp,
                isPortForwardingEnabled = viewModel.isPortForwardingEnabled(),
                portForwardingStatus = viewModel.portForwardingStatus,
                port = viewModel.port.value.toString(),
            )
            Separator()
            QuickSettingsTile(
                onKillSwitchClick = {
                    viewModel.navigateToKillSwitch()
                },
                onAutomationClick = {
                    viewModel.navigateToAutomation()
                },
                onPrivateBrowserClick = {
                    onPrivateBrowserClick(context)
                },
                onMoreClick = {
                    // TODO: https://polymoon.atlassian.net/browse/PIA-465
                },
            )
            Separator()
            QuickConnectTile(
                servers = viewModel.quickConnectServers.value,
                onClick = {
                    viewModel.quickConnect(it)
                },
            )
            Separator()
            FavoritesTile(viewModel.favoriteServers.value)
            Separator()
            if (viewModel.snoozeTime.longValue != 0L && viewModel.snoozeTime.longValue < System.currentTimeMillis()) {
                viewModel.snooze(context, SnoozeInterval.SNOOZE_DEFAULT_MS)
            }
            SnoozeTile(
                viewModel.snoozeState.value,
                onClick = {
                    if (viewModel.isConnectionActive()) {
                        viewModel.snooze(context, it)
                    }
                },
                onResumeClick = {
                    viewModel.snooze(context, SnoozeInterval.SNOOZE_DEFAULT_MS)
                },
            )
            Separator()
            UsageTile(viewModel.download.value, viewModel.upload.value)
            Separator()
            ConnectionInfoTile(viewModel.getConnectionSettings())
        }
    }
}

private fun onPrivateBrowserClick(context: Context) {
    var launchIntent: Intent? =
        context.packageManager.getLaunchIntentForPackage("nu.tommie.inbrowser")
    if (launchIntent != null) {
        val url = "https://play.google.com/store/apps/details?id=nu.tommie.inbrowser"
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        launchIntent.data = Uri.parse(url)
        context.startActivity(launchIntent)
    } else {
        launchIntent = Intent(Intent.ACTION_VIEW)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        launchIntent.data = Uri.parse("market://details?id=" + "nu.tommie.inbrowser")

        //silently fail if Google Play Store isn't installed.
        if (launchIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(launchIntent)
        }
    }
}