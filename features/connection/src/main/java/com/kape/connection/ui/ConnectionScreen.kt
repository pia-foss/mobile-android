package com.kape.connection.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.kape.notifications.data.NotificationChannelManager.Companion.CHANNEL_ID
import com.kape.sidemenu.ui.SideMenuUiDrawer
import com.kape.ui.elements.Separator
import com.kape.ui.theme.Space
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.Locale

@Composable
fun ConnectionScreen() {
    val viewModel: ConnectionViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()
    val locale = Locale.getDefault().language
    val context = LocalContext.current
    val intent: Intent = koinInject()

    LaunchedEffect(key1 = Unit) {
        viewModel.loadServers(locale)
    }

    SideMenuUiDrawer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ConnectionAppBar(
                viewModel = appBarViewModel,
                onHeaderClick = { /*TODO*/ },
                onLeftButtonClick = { openDrawer() }
            )
            Spacer(modifier = Modifier.height(Space.NORMAL))
            ConnectionButton(ConnectionState.Default) {
                viewModel.connect(
                    getNotification(context), PendingIntent.getActivity(
                        context,
                        123,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            }

            state.selectedServer?.let {
                RegionInformationTile(server = it) {
                    viewModel.showRegionSelection()
                }
            }
            Separator()
            // TODO: hardcoded data for display purposes, will be updated when VPN manager is integrated
            IpInformationTile(ip = "91.155.24.17", vpnIp = "---")
            Separator()
            QuickSettingsTile()
            Separator()
            QuickConnectTile(servers = state.quickConnectServers)
            Separator()
            FavoritesTile(state.favoriteServers)
            Separator()
            SnoozeTile(state.snoozeState, viewModel)
            Separator()
            UsageTile(state.usageState)
            Separator()
            ConnectionInfoTile()
        }
    }
}

private fun getNotification(context: Context): Notification {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationBuilder = Notification.Builder(context, CHANNEL_ID)
        notificationBuilder.setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    } else {
        Notification.Builder(context).setCategory(Notification.CATEGORY_SERVICE).build()
    }
}