package com.kape.vpn.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.kape.ui.utils.LocalIsDetailPane
import com.kape.ui.utils.LocalSelectedListItem

/**
 * Shows list and detail entries side by side when the space for screens is landscape-shaped
 * and wide enough for two usable panes; otherwise every entry stays a full screen.
 *
 * [maxWidth] and [maxHeight] are the space NavDisplay gets (after the navigation rail), not the
 * window, so a phone in landscape qualifies once the rail is accounted for.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberListDetailPanesStrategy(
    maxWidth: Dp,
    maxHeight: Dp,
): SceneStrategy<NavKey> {
    val isTwoPane = maxWidth > maxHeight && maxWidth >= MIN_TWO_PANE_WIDTH
    val directive =
        calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()).copy(
            maxHorizontalPartitions = if (isTwoPane) 2 else 1,
            // The panes' app bars meet, reading as one bar
            horizontalPartitionSpacerSize = 0.dp,
            // The list pane's width; the detail pane takes the rest
            defaultPanePreferredWidth = minOf(LIST_PANE_MAX_WIDTH, maxWidth * LIST_PANE_MAX_FRACTION),
        )
    val listDetail =
        rememberListDetailSceneStrategy<NavKey>(
            // One back press undoes one navigation, as everywhere else in the app
            backNavigationBehavior = BackNavigationBehavior.PopUntilCurrentDestinationChange,
            directive = directive,
        )
    return ListRequiredSceneStrategy(listDetail)
}

/**
 * Marks the entry as a list whose items are shown beside it in two-pane layouts, and provides
 * [LocalSelectedListItem] while they are.
 *
 * Items replace each other rather than stacking (see `DestinationNavOptions.PopUpTo`), so the
 * item open beside the list is the entry right above it in [backStack].
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
inline fun <reified K : NavKey> EntryProviderScope<NavKey>.listPaneEntry(
    backStack: List<NavKey>,
    noinline detailPlaceholder: @Composable () -> Unit,
    noinline content: @Composable (K) -> Unit,
) = entry<K>(
    metadata =
        ListDetailSceneStrategy.listPane(detailPlaceholder = { detailPlaceholder() }) +
            (LIST_PANE_KEY to true),
) { key ->
    // The scene scope is only present while the entry is laid out as a pane
    val selected =
        if (LocalListDetailSceneScope.current != null) {
            val listIndex = backStack.indexOfLast { it is K }
            if (listIndex >= 0) backStack.getOrNull(listIndex + 1) else null
        } else {
            null
        }
    CompositionLocalProvider(LocalSelectedListItem provides selected) {
        content(key)
    }
}

/** Marks the entry as an item of a [listPaneEntry], shown beside the list in two-pane layouts. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
inline fun <reified K : NavKey> EntryProviderScope<NavKey>.detailPaneEntry(noinline content: @Composable (K) -> Unit) =
    entry<K>(metadata = ListDetailSceneStrategy.detailPane() + (DETAIL_PANE_KEY to true)) { key ->
        // The scene scope is only present while the entry is laid out as a pane
        CompositionLocalProvider(LocalIsDetailPane provides (LocalListDetailSceneScope.current != null)) {
            content(key)
        }
    }

/**
 * Only lets [delegate] lay out panes when the list is among them. A detail opened from
 * elsewhere (e.g. Protocols from the connection screen's quick settings) would otherwise sit
 * beside an empty list pane; it stays a full screen instead.
 */
private class ListRequiredSceneStrategy<T : Any>(
    private val delegate: SceneStrategy<T>,
) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        // The entries a list-detail scene would take: the run of pane entries at the top
        val paneEntries =
            entries.takeLastWhile { LIST_PANE_KEY in it.metadata || DETAIL_PANE_KEY in it.metadata }
        if (paneEntries.none { LIST_PANE_KEY in it.metadata }) return null
        return with(delegate) { this@calculateScene.calculateScene(entries) }
    }
}

const val LIST_PANE_KEY = "com.kape.vpn.navigation.listPane"
const val DETAIL_PANE_KEY = "com.kape.vpn.navigation.detailPane"

// Below this each pane would be too narrow for settings rows
private val MIN_TWO_PANE_WIDTH = 520.dp
private val LIST_PANE_MAX_WIDTH = 360.dp
private const val LIST_PANE_MAX_FRACTION = 0.4f