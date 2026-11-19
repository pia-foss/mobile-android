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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection.Companion.Left
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import com.kape.appbar.view.topBarColor
import com.kape.appbar.view.tv.TvHomeHeaderItem
import com.kape.contracts.ConnectionStatusSource
import com.kape.data.RegionItemType
import com.kape.regions.vpn.domain.VPN_PING_TIMEOUT_MS
import com.kape.ui.R
import com.kape.ui.Screen
import com.kape.ui.tv.elements.Search
import com.kape.ui.tv.text.RegionSelectionGridSectionText
import com.kape.ui.utils.LocalColors
import com.kape.vpnregionselection.ui.displayIso
import com.kape.vpnregionselection.ui.displayName
import com.kape.vpnregionselection.ui.vm.VpnRegionSelectionViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TvVpnRegionSelectionScreen(viewModel: VpnRegionSelectionViewModel = koinViewModel()) =
    Screen {
        val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]?.language
        val initialFocusRequester = remember { FocusRequester() }
        val allButtonFocusRequester = remember { FocusRequester() }
        val favoriteButtonFocusRequester = remember { FocusRequester() }
        val searchButtonFocusRequester = remember { FocusRequester() }
        val status by koinInject<ConnectionStatusSource>().status.collectAsStateWithLifecycle()
        val isFavoriteSelected = remember { mutableStateOf(false) }
        val isSearchEnabled = remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            initialFocusRequester.requestFocus()
            locale?.let { viewModel.refresh(it, force = false) }
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val hasUpdateAvailable by viewModel.hasUpdateAvailable.collectAsStateWithLifecycle()

        val serverItems =
            when {
                isFavoriteSelected.value -> state.favoriteRegions
                isSearchEnabled.value -> state.searchResults ?: state.allRegions
                else -> state.allRegions
            }

        Box(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 4.dp,
                color = status.topBarColor(LocalColors.current),
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(start = 32.dp, top = 24.dp, end = 32.dp, bottom = 0.dp)
                        .background(LocalColors.current.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                TvHomeHeaderItem(
                    modifier = Modifier.focusRequester(initialFocusRequester),
                    title = stringResource(id = R.string.location_selection_title),
                    connectionStatus = status,
                    defaultSelectedTabIndex = 1,
                    hasUpdateAvailable = hasUpdateAvailable,
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
                    modifier =
                        Modifier
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
                                viewModel.search("")
                            },
                            onAllFocusRequester = allButtonFocusRequester,
                            onFavoriteSelected = {
                                isFavoriteSelected.value = true
                                isSearchEnabled.value = false
                                viewModel.search("")
                            },
                            onFavoriteFocusRequester = favoriteButtonFocusRequester,
                            onSearchSelected = {
                                isFavoriteSelected.value = false
                                isSearchEnabled.value = true
                                viewModel.search("")
                            },
                            onSearchFocusRequester = searchButtonFocusRequester,
                        )
                    }
                    Column(
                        modifier =
                            Modifier
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
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                onTextChanged = viewModel::search,
                            )
                        }
                        if (isFavoriteSelected.value) {
                            if (serverItems.isEmpty()) {
                                RegionSelectionGridSectionText(
                                    content = stringResource(id = R.string.no_favorite_locations_available),
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                                Card(
                                    modifier =
                                        Modifier
                                            .padding(all = 16.dp)
                                            .semantics(mergeDescendants = true) { },
                                    shape = RoundedCornerShape(12.dp),
                                    colors =
                                        CardDefaults.cardColors(
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
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(16.dp),
                        ) {
                            items(serverItems.size) { index ->
                                val serverItem = serverItems[index]
                                when (val type = serverItem.type) {
                                    is RegionItemType.Content -> {
                                        TvLocationPickerItem(
                                            vpnServerIso = type.server.displayIso(),
                                            vpnServerName = type.server.displayName(),
                                            vpnServerLatency = type.server.latency,
                                            vpnServerLatencyTimeout = VPN_PING_TIMEOUT_MS.toString(),
                                            enableFavorite = type.enableFavorite,
                                            isFavorite = type.isFavorite,
                                            isDedicatedIp = type.server.isDedicatedIp,
                                            onClick = { viewModel.connectTo(type.server) },
                                            onLongClick = {
                                                if (type.enableFavorite) viewModel.onFavoriteClicked(type.server)
                                            },
                                        )
                                    }

                                    RegionItemType.HeadingAll -> {}
                                    RegionItemType.HeadingFavorites -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }