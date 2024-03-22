package com.kape.vpnregionselection.ui.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.regions.data.ServerData
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.elements.Search
import com.kape.ui.mobile.text.MenuText
import com.kape.ui.utils.LocalColors
import com.kape.vpnregionselection.ui.vm.VpnRegionSelectionViewModel
import com.kape.vpnregionselection.util.ItemType
import org.koin.androidx.compose.koinViewModel

@Composable
fun VpnRegionSelectionScreen() = Screen {
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]?.language
    val isLoading = remember { mutableStateOf(false) }
    val viewModel: VpnRegionSelectionViewModel =
        koinViewModel<VpnRegionSelectionViewModel>().apply {
            autoRegionIso = stringResource(id = R.string.automatic_iso)
            autoRegionName = stringResource(id = R.string.optimal_vpn_region)
            LaunchedEffect(Unit) {
                locale?.let {
                    loadVpnRegions(it, isLoading, false)
                }
            }
        }
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.location_selection_title))
    }
    val isSearchEnabled = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(LocalColors.current.surfaceVariant)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppBar(appBarViewModel, onLeftIconClick = { viewModel.navigateBack() })
        Column(modifier = Modifier.widthIn(max = 520.dp)) {
            Search(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                hint = stringResource(id = R.string.search),
            ) {
                viewModel.filterByName(it, isSearchEnabled)
            }

            SwipeRefresh(
                state = rememberSwipeRefreshState(isLoading.value),
                onRefresh = {
                    locale?.let {
                        viewModel.loadVpnRegions(locale, isLoading, true)
                    }
                },
            ) {
                LazyColumn {
                    val items = if (isSearchEnabled.value) {
                        viewModel.sorted.value
                    } else {
                        viewModel.servers.value
                    }
                    items(items.size) { index ->
                        val item = items[index]
                        when (item.type) {
                            is ItemType.Content -> {
                                LocationPickerItem(
                                    server = item.type.server,
                                    isFavorite = item.type.isFavorite,
                                    enableFavorite = item.type.enableFavorite,
                                    isPortForwardingEnabled = viewModel.isPortForwardingEnabled,
                                    onClick = { viewModel.onVpnRegionSelected(it) },
                                    onFavoriteVpnClick = {
                                        viewModel.onFavoriteVpnClicked(
                                            ServerData(
                                                item.type.server.name,
                                                item.type.server.isDedicatedIp,
                                            ),
                                        )
                                    },
                                )
                            }

                            ItemType.HeadingAll -> {
                                MenuText(
                                    content = stringResource(id = R.string.all_locations),
                                    modifier = Modifier.padding(16.dp),
                                )
                            }

                            ItemType.HeadingFavorites -> {
                                MenuText(
                                    content = stringResource(id = R.string.favorite),
                                    modifier = Modifier.padding(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}