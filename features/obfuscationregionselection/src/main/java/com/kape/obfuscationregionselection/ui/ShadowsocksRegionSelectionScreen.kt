package com.kape.obfuscationregionselection.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.data.RegionItemType
import com.kape.obfuscationregionselection.ui.vm.ShadowsocksRegionSelectionViewModel
import com.kape.ui.R
import com.kape.ui.Screen
import com.kape.ui.mobile.elements.Search
import com.kape.ui.mobile.text.MenuText
import com.kape.ui.utils.LocalColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShadowsocksRegionSelectionScreen(viewModel: ShadowsocksRegionSelectionViewModel = koinViewModel()) =
    Screen {
        val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]?.language
        val state by viewModel.state.collectAsStateWithLifecycle()
        val appBarViewModel: AppBarViewModel =
            koinViewModel<AppBarViewModel>().apply {
                appBarText(stringResource(id = R.string.location_selection_title))
            }

        Column(
            modifier =
                Modifier
                    .background(LocalColors.current.surfaceVariant)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppBar(viewModel = appBarViewModel)

            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                Search(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    onTextChanged = viewModel::search,
                )

                PullToRefreshBox(
                    state = rememberPullToRefreshState(),
                    isRefreshing = state.isRefreshing,
                    onRefresh = { locale?.let(viewModel::refresh) },
                ) {
                    LazyColumn {
                        items(state.visibleRegions) { item ->
                            when (val type = item.type) {
                                is RegionItemType.Content -> {
                                    LocationPickerItem(
                                        server = type.server,
                                        isFavorite = type.isFavorite,
                                        enableFavorite = type.enableFavorite,
                                        onClick = viewModel::onRegionClicked,
                                        onFavoriteClick = { viewModel.onFavoriteClicked(type.server) },
                                    )
                                }

                                RegionItemType.HeadingAll -> {
                                    MenuText(
                                        content = stringResource(id = R.string.all_locations),
                                        modifier = Modifier.padding(16.dp),
                                    )
                                }

                                RegionItemType.HeadingFavorites -> {
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