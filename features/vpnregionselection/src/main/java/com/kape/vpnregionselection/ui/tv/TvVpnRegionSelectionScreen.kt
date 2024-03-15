package com.kape.vpnregionselection.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import com.kape.appbar.view.tv.TvHomeHeaderItem
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.tv.text.RegionSelectionGridSectionText
import com.kape.ui.tv.tiles.VpnLocationPicker
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnregions.utils.VPN_REGIONS_PING_TIMEOUT
import com.kape.vpnregionselection.ui.vm.VpnRegionSelectionViewModel
import com.kape.vpnregionselection.util.ItemType
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun TvVpnRegionSelectionScreen() = Screen {
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]?.language
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val isSearchEnabled = remember { mutableStateOf(false) }
    val viewModel: VpnRegionSelectionViewModel =
        koinViewModel<VpnRegionSelectionViewModel>().apply {
            autoRegionIso = stringResource(id = R.string.automatic_iso)
            autoRegionName = stringResource(id = R.string.optimal_vpn_region)
            LaunchedEffect(Unit) {
                locale?.let {
                    loadVpnRegions(it, mutableStateOf(false), false)
                }
            }
        }

    val serverItems = viewModel.getTvVpnServers().value

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, top = 24.dp, end = 32.dp, bottom = 0.dp)
                .background(LocalColors.current.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TvHomeHeaderItem(
                title = stringResource(id = R.string.location_selection_title),
                connectionStatus = connectionStatus,
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
            ) {
                Column(modifier = Modifier.weight(0.25f)) {
                }
                Column(
                    modifier = Modifier.weight(0.75f),
                ) {
                    RegionSelectionGridSectionText(
                        content = stringResource(id = R.string.all_locations),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    TvLazyVerticalGrid(
                        columns = TvGridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp),
                    ) {
                        items(serverItems.size) { index ->
                            val serverItem = serverItems[index]
                            when (serverItem.type) {
                                is ItemType.Content -> {
                                    VpnLocationPicker(
                                        vpnServerIso = serverItem.type.server.iso,
                                        vpnServerName = serverItem.type.server.name,
                                        vpnServerLatency = serverItem.type.server.latency,
                                        vpnServerLatencyTimeout = VPN_REGIONS_PING_TIMEOUT.toString(),
                                        isFavorite = serverItem.type.isFavorite,
                                        onClick = {
                                            viewModel.onVpnRegionSelected(serverItem.type.server)
                                        },
                                        onLongClick = {
                                            if (serverItem.type.enableFavorite) {
                                                viewModel.onFavoriteVpnClicked(
                                                    serverItem.type.server.name,
                                                )
                                            }
                                        },
                                    )
                                }
                                ItemType.HeadingAll -> { }
                                ItemType.HeadingFavorites -> { }
                            }
                        }
                    }
                }
            }
        }
    }
}