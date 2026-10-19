package com.kape.ui.mobile.elements

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.kape.contracts.Router
import com.kape.ui.utils.DeviceConfiguration
import org.koin.compose.koinInject

@Composable
fun Screen(content: @Composable () -> Unit) {
    val router: Router = koinInject()

    BackHandler {
        router.navigateBack()
    }
    content()
}

@Composable
fun AdaptiveScreen(
    mobilePortrait: @Composable (modifier: Modifier) -> Unit,
    mobileLandscape: @Composable (modifier: Modifier) -> Unit = mobilePortrait,
    tabletAndDesktop: @Composable (modifier: Modifier) -> Unit = mobilePortrait,
) = Screen {
    val deviceConfiguration =
        DeviceConfiguration.fromWindowSizeClass(currentWindowAdaptiveInfoV2().windowSizeClass)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
    ) { paddingValues ->
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            val rootModifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .semantics { testTagsAsResourceId = true }

            when (deviceConfiguration) {
                DeviceConfiguration.MOBILE_PORTRAIT -> mobilePortrait(rootModifier)
                DeviceConfiguration.MOBILE_LANDSCAPE -> mobileLandscape(rootModifier)
                DeviceConfiguration.TABLET_PORTRAIT,
                DeviceConfiguration.TABLET_LANDSCAPE,
                DeviceConfiguration.DESKTOP,
                -> tabletAndDesktop(rootModifier)
            }
        }
    }
}

/**
 * Picks a layout from the space the content actually gets (after app bars, the
 * navigation rail or a neighbouring pane), not from the window size.
 * Unlike [AdaptiveScreen] it adds no Scaffold, insets or scrolling, so it can be
 * used inside a screen that owns its own chrome.
 *
 * [wide] is used when there is room for side-by-side content, or when the space is
 * landscape-shaped and vertical room is the scarcer resource.
 */
@Composable
fun AdaptiveContent(
    modifier: Modifier = Modifier,
    compact: @Composable BoxWithConstraintsScope.(modifier: Modifier) -> Unit,
    wide: @Composable BoxWithConstraintsScope.(modifier: Modifier) -> Unit = compact,
) {
    // Screens shrink above the keyboard; deciding without it keeps the layout, and the focused
    // field in it, from switching while typing (a portrait phone with the keyboard open is wider
    // than it is tall)
    val keyboardHeight = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    BoxWithConstraints(modifier = modifier) {
        val heightWithoutKeyboard = maxHeight + keyboardHeight
        val isWide = maxWidth.value >= WIDTH_DP_MEDIUM_LOWER_BOUND || maxWidth > heightWithoutKeyboard
        if (isWide) {
            wide(Modifier.fillMaxSize())
        } else {
            compact(Modifier.fillMaxSize())
        }
    }
}

/**
 * A [header] (logo, illustration) and a [body] (text, form, actions): stacked when compact,
 * side by side when wide, the header centred vertically on the left.
 *
 * Every column scrolls when its content is taller than the space and is at least as tall as
 * the space otherwise, so a weighted Spacer in [body] pins what follows it to the bottom.
 * Both are told whether the layout is compact, e.g. to shrink illustrations beside the body.
 * Without [hasBody] (e.g. nothing to list yet) the header takes the whole width when wide.
 */
@Composable
fun AdaptiveSplitContent(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 0.dp,
    centerBodyVertically: Boolean = false,
    hasBody: Boolean = true,
    header: @Composable ColumnScope.(isCompact: Boolean) -> Unit,
    body: @Composable ColumnScope.(isCompact: Boolean) -> Unit,
) {
    AdaptiveContent(
        modifier = modifier,
        compact = { contentModifier ->
            val visibleHeight = maxHeight
            SplitColumn(modifier = contentModifier) {
                AppColumn(
                    modifier =
                        Modifier
                            .heightIn(min = visibleHeight)
                            .padding(horizontal = horizontalPadding),
                ) {
                    header(true)
                    body(true)
                }
            }
        },
        wide = { contentModifier ->
            val visibleHeight = maxHeight
            Row(modifier = contentModifier) {
                SplitColumn(modifier = Modifier.weight(1f)) {
                    AppColumn(
                        modifier =
                            Modifier
                                .heightIn(min = visibleHeight)
                                .padding(horizontal = horizontalPadding),
                        centerVertically = true,
                    ) {
                        header(false)
                    }
                }
                if (hasBody) {
                    SplitColumn(modifier = Modifier.weight(1f)) {
                        AppColumn(
                            modifier =
                                Modifier
                                    .heightIn(min = visibleHeight)
                                    .padding(horizontal = horizontalPadding),
                            centerVertically = centerBodyVertically,
                        ) {
                            body(false)
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun SplitColumn(
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

/**
 * [centerVertically] only has an effect when the column has a bounded height. Inside a
 * scrolling parent, pass `Modifier.heightIn(min = <visible height>)` as [modifier].
 */
@Composable
fun AppColumn(
    modifier: Modifier = Modifier,
    centerVertically: Boolean = false,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                // Cap first: after fillMaxWidth the width is fixed and widthIn can't narrow it
                .widthIn(max = 460.dp)
                .fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = if (centerVertically) Arrangement.Center else Arrangement.Top,
        content = content,
    )
}