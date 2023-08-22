package com.kape.sidemenu.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kape.sidemenu.R
import com.kape.sidemenu.ui.vm.SideMenuViewModel
import com.kape.sidemenu.utils.SideMenuState
import com.kape.ui.theme.ConnectingOrange
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Height
import com.kape.ui.theme.Space
import com.kape.ui.theme.Square
import com.kape.ui.theme.Width
import com.kape.ui.utils.LocalColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale
import kotlin.math.min

interface SideMenuUiDrawerScope {
    fun openDrawer()
    fun closeDrawer()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideMenuUiDrawer(
    screenContent: @Composable SideMenuUiDrawerScope.() -> Unit,
) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)

    val drawerScope: SideMenuUiDrawerScope = remember(drawerState) {
        SideMenuUiDrawerScopeImpl(
            scope = scope,
            drawerState = drawerState,
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
//        drawerShape = DrawerShape(maxSizePx = with(LocalDensity.current) { Width.SIDE_MENU.toPx() }),
        drawerContent = {
            DrawerContent(scope, drawerState)
        },
        content = {
            drawerScope.screenContent()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContent(scope: CoroutineScope, drawerState: DrawerState) {
    val viewModel: SideMenuViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.sideMenuState }.collectAsState()

    LazyColumn(
        modifier = Modifier
            .background(color = LocalColors.current.surface)
            .width(Width.SIDE_MENU)
            .fillMaxHeight(),
        contentPadding = PaddingValues(
            start = 0.dp,
            end = 0.dp,
            top = Space.BIGGER,
            bottom = Space.HUGE,
        ),
    ) {
        item {
            IconAppVersionWithUsernameView(state)

            SeparatorView(
                isSeparatorVisible = false,
                topPadding = Space.BIG,
                bottomPadding = 0.dp,
            )

            if (state.showExpirationNotice) {
                SubscriptionExpiryView(
                    onClick = {
                        // TODO:  "action: open 'purchase' dialog & trigger toast, if account is not renewable"
                    },
                    state.daysRemaining,
                )
            }

            IconTitleView(
                resIcon = R.drawable.ic_drawer_region,
                resTitle = R.string.drawer_item_title_region_selection,
                onClick = {
                    viewModel.navigateToRegionSelection()
                    scope.launch {
                        drawerState.close()
                    }
                },
            )

            IconTitleView(
                resIcon = R.drawable.ic_drawer_account,
                resTitle = R.string.drawer_item_title_account,
                onClick = {
                    viewModel.navigateToProfile()
                    scope.launch {
                        drawerState.close()
                    }
                },
            )

            IconTitleView(
                resIcon = R.drawable.ic_drawer_dip_settings,
                resTitle = R.string.drawer_item_title_dedicated_ip,
                onClick = {
                    // TODO: "action: open 'dedicated ip' screen"
                },
            )

            IconTitleView(
                resIcon = R.drawable.ic_drawer_per_app,
                resTitle = R.string.drawer_item_title_per_app_settings,
                onClick = {
                    // TODO: "action: open 'per app settings' screen"
                },
            )

            IconTitleView(
                resIcon = R.drawable.ic_drawer_settings,
                resTitle = R.string.drawer_item_title_settings,
                onClick = {
                    viewModel.navigateToSettings()
                },
            )

            IconTitleView(
                resIcon = R.drawable.ic_drawer_logout,
                resTitle = R.string.drawer_item_title_logout,
                onClick = {
                    viewModel.logout()
                },
            )

            SeparatorView(
                isSeparatorVisible = true,
                topPadding = Space.MEDIUM,
                bottomPadding = Space.MEDIUM,
            )

            IconTitleView(
                resIcon = R.drawable.ic_drawer_about,
                resTitle = R.string.drawer_item_title_about,
                onClick = {
                    // TODO: "action: open 'about' screen"
                },
            )

            IconTitleView(
                resIcon = R.drawable.ic_privacy_link,
                resTitle = R.string.drawer_item_title_privacy_policy,
                onClick = {
                    // TODO: "action: open 'privacy policy' in 'web view' screen"
                },
            )

            IconTitleView(
                resIcon = R.drawable.ic_drawer_homepage,
                resTitle = R.string.drawer_item_title_homepage,
                onClick = {
                    // TODO: "action: open 'home page' in 'web view' screen"
                },
            )

            IconTitleView(
                resIcon = R.drawable.ic_drawer_support,
                resTitle = R.string.drawer_item_title_contact_support,
                onClick = {
                    // TODO: "action: open 'help desk' in 'web view' screen"
                },
            )
        }
    }
}

@Composable
private fun IconAppVersionWithUsernameView(state: SideMenuState) {
    val appVersionFormat: String =
        stringResource(R.string.drawer_item_description_app_version_format)

    val title: String = state.username
    val description: String = appVersionFormat.format(state.versionName, state.versionCode)

    Row(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.drawer_icon),
            modifier = Modifier
                .align(CenterVertically)
                .padding(start = Space.MEDIUM, top = Space.SMALL, bottom = Space.SMALL)
                .size(Square.DEFAULT),
            contentDescription = stringResource(id = R.string.icon),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(CenterVertically)
                .padding(start = Space.NORMAL, top = Space.SMALL, bottom = Space.SMALL),
        ) {
            // TODO: FIX!
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
//                fontSize = SideMenuUiTheme.PiaTextTitle.textSize,
//                fontFamily = SideMenuUiTheme.PiaTextTitle.fontFamily,
//                style = SideMenuUiTheme.PiaTextTitle.textStyle.copy(
//                    color = SideMenuUiTheme.colors.grey20_white,
//                ),
            )

            Spacer(modifier = Modifier.height(Space.MINI))

            // TODO: FIX!
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = description,
                fontSize = FontSize.Normal,
//                fontFamily = SideMenuUiTheme.PiaTextTitle.fontFamily,
//                style = SideMenuUiTheme.PiaTextTitle.textStyle.copy(
//                    color = SideMenuUiTheme.colors.grey20_white,
//                ),
            )
        }
    }
}

@Composable
private fun IconTitleView(
    @DrawableRes resIcon: Int,
    @StringRes resTitle: Int,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Height.DEFAULT)
            .makeClickable(
                onClick = onClick,
                hasRipple = true,
            ),
    ) {
        Spacer(modifier = Modifier.width(Space.MEDIUM))

        Image(
            modifier = Modifier
                .align(CenterVertically)
                .width(Square.ICON)
                .height(Square.ICON),
            painter = painterResource(id = resIcon),
            contentScale = ContentScale.Inside,
            contentDescription = stringResource(id = R.string.icon),
        )

        Spacer(modifier = Modifier.width(Space.SMALL))

        // TODO: FIX!
        Text(
            text = stringResource(resTitle),
            modifier = Modifier
                .fillMaxWidth()
                .align(CenterVertically),
            fontSize = FontSize.Big,
//                style = SideMenuUiTheme.PiaTextBody2.textStyle.copy(
//                    color = SideMenuUiTheme.PiaTextBody2.textColor,
//                ),
            maxLines = 1,
        )
    }
}

@Composable
private fun SeparatorView(
    isSeparatorVisible: Boolean,
    topPadding: Dp,
    bottomPadding: Dp,
) {
    if (isSeparatorVisible) {
        // TODO: FIX!
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topPadding, bottom = bottomPadding),
//                .background(SideMenuUiTheme.colors.pia_sidemenu_divider),
        )
    } else {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = topPadding + bottomPadding),
        )
    }
}

@Composable
private fun SubscriptionExpiryView(
    onClick: (() -> Unit)?,
    daysRemaining: Int,
) {
    val title: String = when (daysRemaining) {
        1 -> stringResource(R.string.drawer_item_title_subscription_expires_format).format(
            stringResource(R.string.duration_one_day),
        )

        in 2..30 -> stringResource(R.string.drawer_item_title_subscription_expires_format).format(
            stringResource(R.string.duration_x_days).format(
                Locale.ENGLISH,
                daysRemaining,
            ),
        )

        else -> ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = ConnectingOrange)
            .makeClickable(
                onClick = onClick,
                hasRipple = false,
            ),
    ) {
        Spacer(modifier = Modifier.width(Space.NORMAL))

        Image(
            modifier = Modifier
                .align(CenterVertically)
                .padding(vertical = Space.MINI)
                .size(Square.EXPIRY_NOTICE),
            painter = painterResource(id = R.drawable.ic_orange_arrow_circle),
            contentDescription = stringResource(id = R.string.icon),
        )

        Spacer(modifier = Modifier.width(Space.MINI))

        Column(
            modifier = Modifier
                .align(CenterVertically)
                .fillMaxWidth(),
        ) {
            // TODO: FIX!
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title.uppercase(),
                fontSize = FontSize.Small,
//                fontFamily = SideMenuUiTheme.PiaExpirationTitle.fontFamily,
//                style = SideMenuUiTheme.PiaExpirationTitle.textStyle.copy(
//                    color = SideMenuUiTheme.PiaExpirationTitle.textColor,
//                ),
                maxLines = 1,
            )
            // TODO: FIX!
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.drawer_item_description_update_account),
                fontSize = FontSize.Tiny,
//                fontFamily = SideMenuUiTheme.PiaExpirationDescription.fontFamily,
//                style = SideMenuUiTheme.PiaExpirationDescription.textStyle.copy(
//                    color = SideMenuUiTheme.PiaExpirationDescription.textColor,
//                ),
                maxLines = 1,
            )
        }
    }
}

// TODO: Iva! Rewrite the whole thing in subsequent pr
private fun Modifier.makeClickable(
    onClick: (() -> Unit)?,
    hasRipple: Boolean,
): Modifier = composed {
    return@composed when {
        onClick != null -> clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = if (hasRipple) rememberRipple(bounded = true) else null,
            onClick =
            onClick@{
                onClick()
            },
        )

        else -> this
    }
}

private class SideMenuUiDrawerScopeImpl
@OptIn(ExperimentalMaterial3Api::class)
constructor(
    private val scope: CoroutineScope,
    private val drawerState: DrawerState,
) : SideMenuUiDrawerScope {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun openDrawer() {
        scope.launch {
            drawerState.open()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun closeDrawer() {
        scope.launch {
            drawerState.close()
        }
    }
}

private class DrawerShape(
    private val maxSizePx: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = 0f,
                right = min(maxSizePx, size.width),
                bottom = size.height,
            ),
        )
    }
}