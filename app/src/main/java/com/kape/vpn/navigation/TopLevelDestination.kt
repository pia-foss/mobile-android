package com.kape.vpn.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.kape.data.AccountDeleted
import com.kape.data.ComposeDestination
import com.kape.data.Connection
import com.kape.data.DedicatedIpActivateToken
import com.kape.data.PerAppSettings
import com.kape.data.Profile
import com.kape.data.Settings
import com.kape.sidemenu.R as SideMenuR
import com.kape.ui.R as UiR

/**
 * Destinations reachable from the navigation rail on medium and wider windows.
 * Mirrors the side menu entries, with [VPN] added since the rail replaces the drawer.
 */
enum class TopLevelDestination(
    val key: ComposeDestination,
    @DrawableRes val iconId: Int,
    @StringRes val titleId: Int,
    // Whether it stays on the rail in short windows; otherwise it moves into the More menu
    val onShortRail: Boolean,
) {
    VPN(Connection, UiR.drawable.ic_shield, UiR.string.drawer_item_title_vpn, onShortRail = true),
    ACCOUNT(Profile, SideMenuR.drawable.ic_account, UiR.string.drawer_item_title_account, onShortRail = true),
    DEDICATED_IP(
        DedicatedIpActivateToken,
        SideMenuR.drawable.ic_ip,
        UiR.string.drawer_item_title_dedicated_ip,
        onShortRail = false,
    ),
    PER_APP_SETTINGS(
        PerAppSettings,
        SideMenuR.drawable.ic_per_app,
        UiR.string.drawer_item_title_per_app_settings,
        onShortRail = false,
    ),
    SETTINGS(Settings, UiR.drawable.ic_settings, UiR.string.drawer_item_title_settings, onShortRail = true),
    ;

    companion object {
        val root: ComposeDestination = Connection

        // Connection clears the stack, so it is only present once the user is past
        // login and onboarding. Screens pushed on top of it that must stay blocking go here.
        private val excluded = setOf(AccountDeleted::class)

        fun showsNavigation(backStack: List<NavKey>): Boolean {
            val top = backStack.lastOrNull() ?: return false
            return top::class !in excluded && backStack.any { it::class == root::class }
        }

        // A short window (e.g. a phone in landscape) can't fit every item on the rail
        fun railEntries(isShortWindow: Boolean): List<TopLevelDestination> =
            if (isShortWindow) entries.filter { it.onShortRail } else entries

        fun moreMenuEntries(isShortWindow: Boolean): List<TopLevelDestination> = entries - railEntries(isShortWindow).toSet()

        // The nearest top-level entry under the current screen, so sub-screens
        // (e.g. Settings → Protocol) keep their parent selected
        fun selectedFor(backStack: List<NavKey>): TopLevelDestination? =
            backStack.asReversed().firstNotNullOfOrNull { key ->
                entries.firstOrNull { it.key::class == key::class }
            }
    }
}