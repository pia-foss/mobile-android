package com.kape.vpnregionselection.ui.mobile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.data.RegionItemType
import com.kape.ui.R
import com.kape.ui.Screen
import com.kape.ui.mobile.elements.Search
import com.kape.ui.mobile.text.MenuText
import com.kape.ui.utils.LocalColors
import com.kape.vpnregionselection.ui.vm.VpnRegionSelectionViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VpnRegionSelectionScreen(viewModel: VpnRegionSelectionViewModel = koinViewModel()) =
    Screen {
        val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]?.language
        val appBarViewModel: AppBarViewModel =
            koinViewModel<AppBarViewModel>().apply {
                appBarText(stringResource(id = R.string.location_selection_title))
            }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val isPortForwardingEnabled by viewModel.isPortForwardingEnabled.collectAsStateWithLifecycle()
        val pendingLocationChange by viewModel.pendingLocationChange.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val pingingWhileConnectedMessage = stringResource(id = R.string.error_pinging_while_connected)

        LaunchedEffect(Unit) {
            locale?.let { viewModel.refresh(it, force = false) }
        }

        Column(
            modifier =
                Modifier
                    .background(LocalColors.current.surfaceVariant)
                    .fillMaxWidth()
                    .semantics { testTagsAsResourceId = true }
                    .padding(WindowInsets.navigationBars.asPaddingValues()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppBar(viewModel = appBarViewModel)
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                Search(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag(":VpnRegionSelectionScreen:searchBar"),
                    onTextChanged = viewModel::search,
                )

                PullToRefreshBox(
                    modifier = Modifier.testTag(":VpnRegionSelectionScreen:list"),
                    state = rememberPullToRefreshState(),
                    isRefreshing = state.isRefreshing,
                    onRefresh = {
                        if (viewModel.isConnected()) {
                            Toast.makeText(context, pingingWhileConnectedMessage, Toast.LENGTH_SHORT).show()
                        } else {
                            locale?.let { viewModel.refresh(it, force = true) }
                        }
                    },
                ) {
                    LazyColumn {
                        itemsIndexed(state.visibleRegions) { index, item ->
                            when (val type = item.type) {
                                is RegionItemType.Content -> {
                                    LocationPickerItem(
                                        server = type.server,
                                        isFavorite = type.isFavorite,
                                        enableFavorite = type.enableFavorite,
                                        isPortForwardingEnabled = isPortForwardingEnabled,
                                        onClick = viewModel::onRegionClicked,
                                        onFavoriteClick = { viewModel.onFavoriteClicked(type.server) },
                                        testTag = ":VpnRegionSelectionScreen:locationItem_$index",
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
                                        content = stringResource(id = R.string.favorite),
                                        modifier =
                                            Modifier
                                                .padding(16.dp)
                                                .testTag(":VpnRegionSelectionScreen:favoritesHeading"),
                                    )
                                }
                            }
                        }
                    }
                }

                if (pendingLocationChange != null) {
                    LocationChangeDialog(
                        onConnect = viewModel::onLocationChangeConfirmed,
                        onCancel = viewModel::onLocationChangeDismissed,
                    )
                }
            }
        }
    }

@Composable
private fun LocationChangeDialog(
    onConnect: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(onClick = onConnect, modifier = Modifier.testTag(":ChangeLocation:confirm")) {
                Text(text = stringResource(id = R.string.dialog_change_location_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(id = R.string.dialog_change_location_dismiss))
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.dialog_change_location_title),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.dialog_change_location_message),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
    )
}