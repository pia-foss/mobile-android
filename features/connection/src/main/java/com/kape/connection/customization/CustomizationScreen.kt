package com.kape.connection.customization

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.view.mobile.AppBarType
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.connection.ui.tiles.ConnectionTile
import com.kape.connection.ui.tiles.NoTileActions
import com.kape.connection.ui.vm.ConnectionTilesViewModel
import com.kape.customization.data.Element
import com.kape.ui.R
import com.kape.ui.Screen
import com.kape.ui.mobile.elements.Visibility
import com.kape.ui.utils.LocalColors
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun CustomizationScreen(
    viewModel: CustomizationViewModel = koinViewModel(),
    tiles: ConnectionTilesViewModel = koinViewModel(),
) = Screen {
    val appBarViewModel: AppBarViewModel = koinViewModel()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to -> viewModel.onMove(from.index, to.index) }

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
                type = AppBarType.Customization,
                onRightIconClick = viewModel::save,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxWidth()
                    .testTag(":CustomizationScreen:elements_list")
                    .semantics { testTagsAsResourceId = true },
            state = lazyListState,
        ) {
            items(items, key = { it.name }) { item ->
                ReorderableItem(reorderableState, key = item.name) { dragging ->
                    val elevation by animateDpAsState(if (dragging) 8.dp else 0.dp)
                    if (item.element != Element.ShadowsocksRegionSelection || item.shouldDisplayElement) {
                        Column(
                            modifier =
                                Modifier
                                    .shadow(elevation)
                                    .fillMaxWidth()
                                    .background(LocalColors.current.surface),
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .longPressDraggableHandle()
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Visibility(
                                    isChecked = item.isVisible,
                                    modifier =
                                        Modifier
                                            .testTag(":CustomizationScreen:visibility_${item.name}")
                                            .clickable { viewModel.toggleVisibility(item.element) },
                                )
                                ConnectionTile(
                                    element = item.element,
                                    tiles = tiles,
                                    actions = NoTileActions,
                                    modifier = Modifier.weight(1f),
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_drag),
                                    contentDescription = null,
                                    tint = LocalColors.current.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}