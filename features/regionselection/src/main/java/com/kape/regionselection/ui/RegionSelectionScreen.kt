package com.kape.regionselection.ui

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
import androidx.compose.material3.Divider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.core.os.ConfigurationCompat
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kape.regionselection.R
import com.kape.regionselection.ui.vm.RegionSelectionViewModel
import com.kape.regionselection.utils.IDLE
import com.kape.regionselection.utils.LOADING
import com.kape.ui.elements.SearchBar
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Space
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegionSelectionScreen() {
    val viewModel: RegionSelectionViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]?.language
    val searchTextState = remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(Unit) {
        locale?.let {
            viewModel.loadRegions(it)
        }
    }
    Column {
        // TODO: Iva! Add AppBar

        when (state) {
            IDLE -> {
            }

            LOADING -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            else -> {
                // state loaded
                SearchBar(searchTextState = searchTextState)
                viewModel.initAutoRegion(
                    stringResource(id = R.string.automatic),
                    stringResource(id = R.string.automatic_iso)
                )
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing = state.loading),
                    onRefresh = {
                        locale?.let {
                            viewModel.loadRegions(locale)
                        }
                    }
                ) {
                    viewModel.filterByName(searchTextState.value.text)
                    LazyColumn {
                        items(state.regions.size) { index ->
                            ServerListItem(
                                server = state.regions[index],
                                isFavorite = remember {
                                    mutableStateOf(viewModel.isServerFavorite(state.regions[index].name))
                                },
                                onClick = {
                                    viewModel.onRegionSelected(it)
                                },
                                onFavoriteClick = {
                                    viewModel.onFavoriteClicked(it)
                                }
                            )
                            Divider(color = LocalColors.current.outline)
                        }
                    }
                }

                if (state.showSortingOptions) {
                    SortingOptions(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SortingOptions(viewModel: RegionSelectionViewModel) {
    val sortBySelectedOption: MutableState<RegionSelectionViewModel.SortByOption> = remember {
        viewModel.sortBySelectedOption
    }
    AlertDialog(onDismissRequest = {
        viewModel.hideSortingOptions()
    }, title = {
            Text(text = stringResource(id = R.string.sort_regions_title), fontSize = FontSize.Title)
        }, text = {
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
                                role = Role.RadioButton
                            )
                            .padding(vertical = Space.MINI)
                    ) {
                        RadioButton(
                            selected = (options.indexOf(it) == sortBySelectedOption.value.index),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = LocalColors.current.primary),
                            modifier = Modifier
                                .padding(end = Space.SMALL)
                                .align(CenterVertically)
                        )
                        Text(
                            text = it,
                            modifier = Modifier
                                .align(CenterVertically)
                        )
                    }
                }
            }
        }, confirmButton = {
            TextButton(onClick = {
                viewModel.sortBy(sortBySelectedOption.value)
            }) {
                Text(
                    text = stringResource(id = R.string.ok),
                    fontSize = FontSize.Normal,
                    color = LocalColors.current.primary
                )
            }
        }, dismissButton = {
            TextButton(onClick = { viewModel.hideSortingOptions() }) {
                Text(
                    text = stringResource(id = R.string.cancel).toUpperCase(Locale.current),
                    fontSize = FontSize.Normal,
                    color = LocalColors.current.primary
                )
            }
        })
}