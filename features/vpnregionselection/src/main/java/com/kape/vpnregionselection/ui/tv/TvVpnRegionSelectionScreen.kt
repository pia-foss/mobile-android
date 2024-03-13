package com.kape.vpnregionselection.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.tv.TvHomeHeaderItem
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionManager
import org.koin.compose.koinInject

@Composable
fun TvVpnRegionSelectionScreen() = Screen {
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()

    Box(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .background(LocalColors.current.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TvHomeHeaderItem(
                title = stringResource(id = com.kape.ui.R.string.location_selection_title),
                connectionStatus = connectionStatus,
            )
            Column(modifier = Modifier.fillMaxSize()) { }
        }
    }
}