package com.kape.region_selection.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import com.kape.region_selection.R
import com.kape.region_selection.ui.vm.RegionSelectionViewModel
import com.kape.region_selection.utils.IDLE
import com.kape.region_selection.utils.LOADING
import com.kape.uicomponents.components.AppBar
import com.kape.uicomponents.components.AppBarColors
import com.kape.uicomponents.components.AppBarState
import com.kape.uicomponents.components.SearchBar
import com.kape.uicomponents.theme.*
import org.koin.androidx.compose.viewModel

@Composable
fun RegionSelectionScreen() {
    PIATheme {
        val viewModel: RegionSelectionViewModel by viewModel()
        val state by remember(viewModel) { viewModel.state }.collectAsState()
        val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0].language
        val searchTextState = remember { mutableStateOf(TextFieldValue("")) }

        LaunchedEffect(Unit) {
            viewModel.loadRegions(locale)
        }

        Column {
            AppBar(onClick = {
                viewModel.navigateBack()
            },
                state = AppBarState(
                    stringResource(id = R.string.region_selection_title),
                    AppBarColors.Default,
                    showLogo = false,
                    showMenu = false,
                    showOverflow = true
                ),
                onOverflowClick = {
                    viewModel.showSortingOptions()
                })

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
                    viewModel.initAutoRegion(stringResource(id = R.string.automatic), stringResource(id = R.string.automatic_iso))
                    SwipeRefresh(state = rememberSwipeRefreshState(isRefreshing = state.loading), onRefresh = {
                        viewModel.loadRegions(locale)
                    }) {
                        viewModel.filterByName(searchTextState.value.text)
                        LazyColumn {
                            items(state.regions.size) { index ->
                                ServerListItem(
                                    server = state.regions[index],
                                    isFavorite = mutableStateOf(viewModel.isServerFavorite(state.regions[index].name)),
                                    onClick = {
                                        viewModel.onRegionSelected(it)
                                    },
                                    onFavoriteClick = {
                                        viewModel.onFavoriteClicked(it)
                                    })
                                Divider(color = Grey85)
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
                                sortBySelectedOption.value = viewModel.getSortingOption(options.indexOf(it))
                            },
                            role = Role.RadioButton
                        )
                        .padding(vertical = Space.MINI)
                ) {
                    RadioButton(
                        selected = (options.indexOf(it) == sortBySelectedOption.value.index),
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = DarkGreen20),
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
            Text(text = stringResource(id = R.string.ok), fontSize = FontSize.Normal, color = DarkGreen20)
        }
    }, dismissButton = {
        TextButton(onClick = { viewModel.hideSortingOptions() }) {
            Text(text = stringResource(id = R.string.cancel).toUpperCase(Locale.current), fontSize = FontSize.Normal, color = DarkGreen20)
        }
    })
}