package com.kape.ui.mobile.elements

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
    BoxWithConstraints(modifier = modifier) {
        val isWide = maxWidth.value >= WIDTH_DP_MEDIUM_LOWER_BOUND || maxWidth > maxHeight
        if (isWide) {
            wide(Modifier.fillMaxSize())
        } else {
            compact(Modifier.fillMaxSize())
        }
    }
}

/**
 * [centerVertically] only has an effect when the column has a bounded height. Inside a
 * scrolling parent, pass `Modifier.heightIn(min = <visible height>)` as [modifier].
 */
@Composable
fun AppColumn(
    modifier: Modifier = Modifier,
    centerVertically: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (centerVertically) Arrangement.Center else Arrangement.Top,
    ) {
        content()
    }
}