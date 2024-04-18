package com.kape.obfuscationregionselection.ui

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
import com.kape.obfuscationregionselection.ui.vm.ShadowsocksRegionSelectionViewModel
import com.kape.obfuscationregionselection.util.ItemType
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.elements.Search
import com.kape.ui.mobile.text.MenuText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun ShadowsocksRegionSelectionScreen() = Screen {
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]?.language
    val isLoading = remember { mutableStateOf(false) }
    val viewModel: ShadowsocksRegionSelectionViewModel =
        koinViewModel<ShadowsocksRegionSelectionViewModel>().apply {
            LaunchedEffect(Unit) {
                locale?.let {
                    getShadowsocksRegions()
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
            ) {
                viewModel.filterByName(it, isSearchEnabled)
            }

            SwipeRefresh(
                state = rememberSwipeRefreshState(isLoading.value),
                onRefresh = {
                    locale?.let {
                        viewModel.fetchShadowsocksRegions(locale, isLoading)
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
                                    onClick = { viewModel.onShadowsocksRegionSelected(it) },
                                    onFavoriteShadowsocksClick = {
                                        viewModel.onFavoriteShadowsocksClicked(
                                            it,
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
                                    content = stringResource(id = R.string.favorites),
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