package com.kape.vpnregionselection.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.ConfigurationCompat
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kape.appbar.view.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.ui.elements.Screen
import com.kape.ui.elements.Search
import com.kape.ui.text.MenuText
import com.kape.ui.utils.LocalColors
import com.kape.vpnregionselection.R
import com.kape.vpnregionselection.ui.vm.VpnRegionSelectionViewModel
import com.kape.vpnregionselection.util.ItemType
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegionSelectionScreen() = Screen {
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]?.language
    val isLoading = remember { mutableStateOf(false) }
    val viewModel: VpnRegionSelectionViewModel =
        koinViewModel<VpnRegionSelectionViewModel>().apply {
            autoRegionIso = stringResource(id = R.string.automatic_iso)
            autoRegionName = stringResource(id = R.string.automatic)
            LaunchedEffect(Unit) {
                locale?.let {
                    loadVpnRegions(it, isLoading)
                }
            }
        }
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.region_selection_title))
    }
    val showSortingOptions = remember { mutableStateOf(false) }
    val isSearchEnabled = remember { mutableStateOf(false) }

    Column(modifier = Modifier.background(LocalColors.current.surfaceVariant)) {
        AppBar(appBarViewModel, onLeftIconClick = { viewModel.navigateBack() })

        if (isLoading.value) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        Search(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            hint = stringResource(id = com.kape.ui.R.string.search),
        ) {
            viewModel.filterByName(it, isSearchEnabled)
        }

        SwipeRefresh(
            state = rememberSwipeRefreshState(isLoading.value),
            onRefresh = {
                locale?.let {
                    viewModel.loadVpnRegions(locale, isLoading)
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
                                onClick = { viewModel.onVpnRegionSelected(it) },
                                onFavoriteVpnClick = { viewModel.onFavoriteVpnClicked(it) },
                            )
                        }

                        ItemType.HeadingAll -> {
                            MenuText(
                                content = stringResource(id = R.string.all_regions),
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

        if (showSortingOptions.value) {
            SortingOptions(viewModel = viewModel, showSortingOptions)
        }
    }
}

@Composable
fun SortingOptions(
    viewModel: VpnRegionSelectionViewModel,
    showSortingOptions: MutableState<Boolean>,
) {
    val sortBySelectedOption: MutableState<VpnRegionSelectionViewModel.SortByOption> = remember {
        viewModel.sortBySelectedOption
    }
    AlertDialog(
        onDismissRequest = {
            showSortingOptions.value = false
        },
        title = {
            Text(text = stringResource(id = R.string.sort_regions_title), fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val options = stringArrayResource(id = R.array.sorting_options)
                options.forEach {
                    Row(
                        verticalAlignment = CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (options.indexOf(it) == sortBySelectedOption.value.index),
                                onClick = {
                                    sortBySelectedOption.value =
                                        viewModel.getSortingOption(options.indexOf(it))
                                },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = (options.indexOf(it) == sortBySelectedOption.value.index),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = LocalColors.current.primary),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .align(CenterVertically),
                        )
                        Text(
                            text = it,
                            modifier = Modifier
                                .align(CenterVertically),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.sortBy(sortBySelectedOption.value)
                },
            ) {
                Text(
                    text = stringResource(id = R.string.ok),
                    fontSize = 14.sp,
                    color = LocalColors.current.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { showSortingOptions.value = false }) {
                Text(
                    text = stringResource(id = R.string.cancel).toUpperCase(Locale.current),
                    fontSize = 14.sp,
                    color = LocalColors.current.primary,
                )
            }
        },
    )
}