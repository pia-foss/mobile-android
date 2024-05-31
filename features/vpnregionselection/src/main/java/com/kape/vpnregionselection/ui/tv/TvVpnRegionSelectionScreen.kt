@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.kape.vpnregionselection.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection.Companion.Left
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.kape.appbar.view.tv.TvHomeHeaderItem
import com.kape.regions.data.ServerData
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.elements.Search
import com.kape.ui.theme.statusBarConnected
import com.kape.ui.theme.statusBarConnecting
import com.kape.ui.theme.statusBarDefault
import com.kape.ui.theme.statusBarError
import com.kape.ui.tv.text.RegionSelectionGridSectionText
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import com.kape.vpnregions.utils.VPN_REGIONS_PING_TIMEOUT
import com.kape.vpnregionselection.ui.vm.VpnRegionSelectionViewModel
import com.kape.vpnregionselection.util.ItemType
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TvVpnRegionSelectionScreen() = Screen {
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]?.language
    val initialFocusRequester = remember { FocusRequester() }
    val allButtonFocusRequester = remember { FocusRequester() }
    val favoriteButtonFocusRequester = remember { FocusRequester() }
    val searchButtonFocusRequester = remember { FocusRequester() }
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val isFavoriteSelected = remember { mutableStateOf(false) }
    val isSearchEnabled = remember { mutableStateOf(false) }
    val viewModel: VpnRegionSelectionViewModel =
        koinViewModel<VpnRegionSelectionViewModel>().apply {
            autoRegionIso = stringResource(id = R.string.automatic_iso)
            autoRegionName = stringResource(id = R.string.optimal_vpn_region)
            LaunchedEffect(Unit) {
                initialFocusRequester.requestFocus()
                locale?.let {
                    loadVpnRegions(it, mutableStateOf(false), false)
                }
            }
        }

    val serverItems = if (isFavoriteSelected.value) {
        viewModel.getTvVpnServers().value.filter {
            it.type is ItemType.Content && it.type.isFavorite
        }
    } else {
        if (isSearchEnabled.value && viewModel.getTvSearchVpnServers().value.isEmpty().not()) {
            viewModel.getTvSearchVpnServers().value
        } else {
            viewModel.getTvVpnServers().value
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 4.dp,
            color = getTopBarConnectionColor(
                status = connectionStatus.value,
                scheme = LocalColors.current,
            ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, top = 24.dp, end = 32.dp, bottom = 0.dp)
                .background(LocalColors.current.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TvHomeHeaderItem(
                modifier = Modifier.focusRequester(initialFocusRequester),
                title = stringResource(id = R.string.location_selection_title),
                connectionStatus = connectionStatus,
                defaultSelectedTabIndex = 1,
                onVpnSelected = {
                    viewModel.navigateToVpn()
                },
                onSettingsSelected = {
                    viewModel.navigateToSideMenu()
                },
                onHelpSelected = {
                    viewModel.navigateToHelp()
                },
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
            ) {
                Column(
                    modifier = Modifier.weight(0.25f),
                ) {
                    TvColumnSelectionItem(
                        modifier = Modifier.padding(top = 16.dp),
                        onAllSelected = {
                            isFavoriteSelected.value = false
                            isSearchEnabled.value = false
                        },
                        onAllFocusRequester = allButtonFocusRequester,
                        onFavoriteSelected = {
                            isFavoriteSelected.value = true
                            isSearchEnabled.value = false
                        },
                        onFavoriteFocusRequester = favoriteButtonFocusRequester,
                        onSearchSelected = {
                            isFavoriteSelected.value = false
                            isSearchEnabled.value = true
                        },
                        onSearchFocusRequester = searchButtonFocusRequester,
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(0.75f)
                        .focusProperties {
                            exit = { focusDirection ->
                                when (focusDirection) {
                                    Left -> {
                                        if (isFavoriteSelected.value) {
                                            favoriteButtonFocusRequester
                                        } else if (isSearchEnabled.value) {
                                            searchButtonFocusRequester
                                        } else {
                                            allButtonFocusRequester
                                        }
                                    }
                                    else -> FocusRequester.Default
                                }
                            }
                        },
                ) {
                    if (isSearchEnabled.value) {
                        Search(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            viewModel.filterByName(it, isSearchEnabled)
                        }
                    }
                    if (isFavoriteSelected.value) {
                        if (serverItems.isEmpty()) {
                            RegionSelectionGridSectionText(
                                content = stringResource(id = R.string.no_favorite_locations_available),
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                            Card(
                                modifier = Modifier
                                    .padding(all = 16.dp)
                                    .semantics(mergeDescendants = true) { },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = LocalColors.current.primaryContainer,
                                ),
                            ) {
                                Text(
                                    modifier = Modifier.padding(all = 16.dp),
                                    color = LocalColors.current.onSurface,
                                    text = stringResource(id = R.string.no_favorite_message),
                                )
                            }
                        }
                    }
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
                                    TvLocationPickerItem(
                                        vpnServerIso = serverItem.type.server.iso,
                                        vpnServerName = serverItem.type.server.name,
                                        vpnServerLatency = serverItem.type.server.latency,
                                        vpnServerLatencyTimeout = VPN_REGIONS_PING_TIMEOUT.toString(),
                                        enableFavorite = serverItem.type.enableFavorite,
                                        isFavorite = serverItem.type.isFavorite,
                                        isDedicatedIp = serverItem.type.server.isDedicatedIp,
                                        onClick = {
                                            viewModel.onVpnRegionSelected(serverItem.type.server)
                                        },
                                        onLongClick = {
                                            if (serverItem.type.enableFavorite) {
                                                viewModel.onFavoriteVpnClicked(
                                                    ServerData(
                                                        serverItem.type.server.name,
                                                        serverItem.type.server.isDedicatedIp,
                                                    ),
                                                )
                                            }
                                        },
                                    )
                                }

                                ItemType.HeadingAll -> {}
                                ItemType.HeadingFavorites -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getTopBarConnectionColor(status: ConnectionStatus, scheme: ColorScheme): Color {
    return when (status) {
        ConnectionStatus.ERROR -> scheme.statusBarError()
        ConnectionStatus.CONNECTED -> scheme.statusBarConnected()
        ConnectionStatus.DISCONNECTED, ConnectionStatus.DISCONNECTING -> scheme.statusBarDefault(scheme)
        ConnectionStatus.RECONNECTING, ConnectionStatus.CONNECTING -> scheme.statusBarConnecting()
    }
}