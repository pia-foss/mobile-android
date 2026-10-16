package com.kape.vpn.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationItemColors
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.kape.appbar.view.mobile.AppBarBackground
import com.kape.appbar.view.mobile.AppBarHeight
import com.kape.router.navigateToTopLevel
import com.kape.sidemenu.ui.screens.mobile.MoreMenuDestination
import com.kape.sidemenu.ui.screens.mobile.SideMenuMoreMenu
import com.kape.ui.theme.brandGreen
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.LocalNavigationSuiteVisible
import org.koin.androidx.compose.koinViewModel
import com.kape.ui.R as UiR

/**
 * App-level navigation: a rail on medium and wider windows once the user is past
 * onboarding. Compact windows and TV keep their existing in-screen navigation.
 */
@Composable
fun AppNavigationSuite(
    backStack: NavBackStack<NavKey>,
    isTv: Boolean,
    content: @Composable () -> Unit,
) {
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val showNavigation =
        !isTv &&
            adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) &&
            TopLevelDestination.showsNavigation(backStack)
    val selected = TopLevelDestination.selectedFor(backStack)
    var moreExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showNavigation) {
        if (!showNavigation) moreExpanded = false
    }

    val isShortWindow = !adaptiveInfo.windowSizeClass.isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND)
    val navigationSuiteType =
        when {
            !showNavigation -> NavigationSuiteType.None
            // Material's default is a bottom bar here, which would take the little height
            // a short window (e.g. a phone in landscape) has
            isShortWindow -> NavigationSuiteType.WideNavigationRailCollapsed
            else -> NavigationSuiteScaffoldDefaults.navigationSuiteType(adaptiveInfo)
        }
    val railEntries = TopLevelDestination.railEntries(isShortWindow)
    val moreMenuEntries = TopLevelDestination.moreMenuEntries(isShortWindow)
    val itemColors = navigationItemColors()
    // Shift rail items below the app bar band; an offset rather than a spacer, since the rail
    // gives every child at least an item's height
    val itemModifier =
        if (navigationSuiteType.isVertical()) {
            Modifier.offset(y = AppBarHeight + RAIL_GAP_BELOW_APP_BAR - RAIL_TOP_SPACE)
        } else {
            Modifier
        }

    CompositionLocalProvider(LocalNavigationSuiteVisible provides showNavigation) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            // Continues the screen's app bar above the rail; the rail is transparent so it shows
            // through, while the opaque content keeps it from showing behind the screen itself
            if (showNavigation) AppBarBackground(viewModel = koinViewModel())

            NavigationSuiteScaffold(
                navigationSuiteType = navigationSuiteType,
                navigationSuiteColors =
                    NavigationSuiteDefaults.colors(
                        wideNavigationRailColors =
                            WideNavigationRailDefaults.colors(containerColor = Color.Transparent),
                        navigationRailContainerColor = Color.Transparent,
                    ),
                containerColor = Color.Transparent,
                navigationItems = {
                    railEntries.forEach { destination ->
                        NavigationSuiteItem(
                            selected = destination == selected,
                            onClick = {
                                backStack.navigateToTopLevel(destination.key, root = TopLevelDestination.root)
                            },
                            icon = { Icon(painterResource(destination.iconId), contentDescription = null) },
                            label = { Text(stringResource(destination.titleId)) },
                            navigationSuiteType = navigationSuiteType,
                            colors = itemColors,
                            modifier = itemModifier.testTag(":NavigationRail:${destination.name}"),
                        )
                    }
                    NavigationSuiteItem(
                        // Stands in for the destinations it holds
                        selected = selected in moreMenuEntries,
                        onClick = { moreExpanded = true },
                        icon = {
                            Box {
                                Icon(painterResource(UiR.drawable.ic_more_horizontal), contentDescription = null)
                                SideMenuMoreMenu(
                                    expanded = moreExpanded,
                                    onDismiss = { moreExpanded = false },
                                    destinations =
                                        moreMenuEntries.map { destination ->
                                            MoreMenuDestination(
                                                iconId = destination.iconId,
                                                titleId = destination.titleId,
                                                testTag = ":MoreMenu:${destination.name}",
                                                onClick = {
                                                    backStack.navigateToTopLevel(
                                                        destination.key,
                                                        root = TopLevelDestination.root,
                                                    )
                                                },
                                            )
                                        },
                                )
                            }
                        },
                        label = { Text(stringResource(UiR.string.drawer_item_title_more)) },
                        navigationSuiteType = navigationSuiteType,
                        colors = itemColors,
                        modifier = itemModifier.testTag(":NavigationRail:MORE"),
                    )
                },
            ) {
                // Screens can draw outside their bounds (e.g. ConnectionScreen's closed modal
                // drawer sits just left of its content), which would cover the rail
                Box(
                    modifier =
                        Modifier
                            .clipToBounds()
                            .background(MaterialTheme.colorScheme.background),
                ) {
                    content()
                }
            }
        }
    }
}

// The PIA color schemes leave secondaryContainer unspecified, which the Material defaults
// use for the selected indicator, so the selection would otherwise be invisible
@Composable
private fun navigationItemColors(): NavigationItemColors {
    val colors = LocalColors.current
    return NavigationItemColors(
        selectedIconColor = Color.White,
        selectedTextColor = colors.onSurface,
        selectedIndicatorColor = colors.brandGreen(),
        unselectedIconColor = colors.onSurfaceVariant,
        unselectedTextColor = colors.onSurfaceVariant,
        disabledIconColor = colors.onSurfaceVariant.copy(alpha = DISABLED_ALPHA),
        disabledTextColor = colors.onSurfaceVariant.copy(alpha = DISABLED_ALPHA),
    )
}

private fun NavigationSuiteType.isVertical(): Boolean =
    this == NavigationSuiteType.WideNavigationRailCollapsed ||
        this == NavigationSuiteType.WideNavigationRailExpanded ||
        this == NavigationSuiteType.NavigationRail

private const val DISABLED_ALPHA = 0.38f

// Material's space between the top of the rail (below the status bar) and its first item
private val RAIL_TOP_SPACE = 44.dp
private val RAIL_GAP_BELOW_APP_BAR = 16.dp