package com.kape.region_selection.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
import com.kape.region_selection.R
import com.kape.region_selection.ui.vm.RegionSelectionViewModel
import com.kape.region_selection.utils.IDLE
import com.kape.region_selection.utils.LOADING
import com.kape.uicomponents.components.AppBar
import com.kape.uicomponents.components.AppBarColors
import com.kape.uicomponents.components.AppBarState
import com.kape.uicomponents.components.SearchBar
import com.kape.uicomponents.theme.DarkGreen20
import com.kape.uicomponents.theme.FontSize
import com.kape.uicomponents.theme.Grey85
import com.kape.uicomponents.theme.PIATheme
import com.kape.uicomponents.theme.Space
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegionSelectionScreen() {
    PIATheme {
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
            AppBar(
                onClick = {
                    viewModel.navigateBack()
                },
                state = AppBarState(
                    stringResource(id = R.string.region_selection_title),
                    AppBarColors.Default,
                    showLogo = false,
                    showMenu = false,
                    showOverflow = true,
                ),
                onOverflowClick = {
                    viewModel.showSortingOptions()
                },
            )

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
                        stringResource(id = R.string.automatic_iso),
                    )
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(isRefreshing = state.loading),
                        onRefresh = {
                            locale?.let {
                                viewModel.loadRegions(locale)
                            }
                        },
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
                                    },
                                )
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
                                sortBySelectedOption.value =
                                    viewModel.getSortingOption(options.indexOf(it))
                            },
                            role = Role.RadioButton,
                        )
                        .padding(vertical = Space.MINI),
                ) {
                    RadioButton(
                        selected = (options.indexOf(it) == sortBySelectedOption.value.index),
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = DarkGreen20),
                        modifier = Modifier
                            .padding(end = Space.SMALL)
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
    }, confirmButton = {
        TextButton(onClick = {
            viewModel.sortBy(sortBySelectedOption.value)
        }) {
            Text(
                text = stringResource(id = R.string.ok),
                fontSize = FontSize.Normal,
                color = DarkGreen20,
            )
        }
    }, dismissButton = {
        TextButton(onClick = { viewModel.hideSortingOptions() }) {
            Text(
                text = stringResource(id = R.string.cancel).toUpperCase(Locale.current),
                fontSize = FontSize.Normal,
                color = DarkGreen20,
            )
        }
    })
}
