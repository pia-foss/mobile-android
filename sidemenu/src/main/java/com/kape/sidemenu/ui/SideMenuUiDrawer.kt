package com.kape.sidemenu.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.kape.sidemenu.R
import com.kape.sidemenu.di.sideMenuModule
import com.kape.sidemenu.models.*
import com.kape.sidemenu.ui.vm.SideMenuViewModel
import com.kape.uicomponents.theme.CommonSideMenuColors
import com.kape.uicomponents.theme.SideMenuRippleTheme
import com.kape.uicomponents.theme.SideMenuUiTheme
import com.kape.uicomponents.theme.Space
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import java.util.*
import kotlin.math.min
import org.koin.core.context.GlobalContext
import java.time.Duration
import java.time.LocalDateTime

private const val TAG: String = "SideMenuUi"

//<editor-fold desc="# Public api #">
interface SideMenuUiDrawerScope {
    fun openDrawer()
    fun closeDrawer()
}

@Composable
fun SideMenuUiDrawer(
        screenContent: @Composable SideMenuUiDrawerScope.() -> Unit
) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)

    val drawerScope: SideMenuUiDrawerScope = remember(drawerState) {
        SideMenuUiDrawerScopeImpl(
                scope = scope,
                drawerState = drawerState
        )
    }

    val dp400: Float = with(LocalDensity.current) { 400.dp.toPx() }

    ModalDrawer(
            drawerState = drawerState,
            drawerShape = DrawerShape(maxSizePx = dp400),
            drawerContent = {
                DrawerContent()
            },
            content = {
                drawerScope.screenContent()
            }
    )
}
//</editor-fold>

//<editor-fold desc="# Test screen #">
@Composable
@Preview
fun TestSideMenuScreen() {
    setupKoinDependencyInjection()

    SideMenuUiDrawer {
        Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Red
        ) {
            Column(
                    modifier = Modifier.fillMaxSize()
            ) {
                TopBar(
                        title = "Side menu test",
                        buttonIcon = Icons.Filled.Menu,
                        onButtonClicked = { openDrawer() }
                )
            }
        }
    }
}

@Composable
@Suppress(names = ["SameParameterValue"])
private fun TopBar(
        title: String,
        buttonIcon: ImageVector,
        onButtonClicked: () -> Unit
) {
    TopAppBar(
            modifier = Modifier.wrapContentHeight(),
            title = {
                Text(
                        text = title
                )
            },
            navigationIcon = {
                IconButton(onClick = { onButtonClicked() }) {
                    Icon(buttonIcon, contentDescription = "")
                }
            },
            backgroundColor = MaterialTheme.colors.primaryVariant
    )
}

@Composable
@SuppressLint("ComposableNaming")
private fun setupKoinDependencyInjection() {
    if (GlobalContext.getKoinApplicationOrNull() != null) {
        return
    }

    val app: Context = LocalContext.current.applicationContext
    if (app::class != Application::class) {
        return
    }

    startKoin {
        androidContext(androidContext = app)
        modules(mutableListOf<Module>().apply {
            add(sideMenuModule)
        })
    }
}
//</editor-fold>

//<editor-fold desc="# internals #">
@Composable
private fun DrawerContent() {
    val viewModel: SideMenuViewModel by viewModel()

    LazyColumn(
            modifier = Modifier
                    .background(color = SideMenuUiTheme.colors.sideMenuBackground)
                    .fillMaxSize(),
            contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = Space.BIGGER, bottom = Space.HUGE)
    ) {
        item {
            IconAppVersionWithUsernameView()

            SeparatorView(
                    isSeparatorVisible = false,
                    topPadding = Space.BIG,
                    bottomPadding = 0.dp
            )

            if (viewModel.isLoggedIn) {
                if (viewModel.showExpiryItem) {
                    SubscriptionExpiryView(
                            onClick = {
                                Log.i(TAG, "action: open 'purchase' dialog & trigger toast, if account is not renewable")
                            }
                    )
                }

                IconTitleView(
                        resIcon = R.drawable.ic_drawer_region,
                        resTitle = R.string.drawer_item_title_region_selection,
                        onClick = {
                            Log.i(TAG, "action: open 'region selection' screen")
                        }
                )

                IconTitleView(
                        resIcon = R.drawable.ic_drawer_account,
                        resTitle = R.string.drawer_item_title_account,
                        onClick = {
                            Log.i(TAG, "action: open 'account info' screen")
                        }
                )

                IconTitleView(
                        resIcon = R.drawable.ic_drawer_dip_settings,
                        resTitle = R.string.drawer_item_title_dedicated_ip,
                        onClick = {
                            Log.i(TAG, "action: open 'dedicated ip' screen")
                        }
                )

                IconTitleView(
                        resIcon = R.drawable.ic_drawer_per_app,
                        resTitle = R.string.drawer_item_title_per_app_settings,
                        onClick = {
                            Log.i(TAG, "action: open 'per app settings' screen")
                        }
                )

                IconTitleView(
                        resIcon = R.drawable.ic_drawer_settings,
                        resTitle = R.string.drawer_item_title_settings,
                        onClick = {
                            Log.i(TAG, "action: open 'settings' screen")
                        }
                )

                IconTitleView(
                        resIcon = R.drawable.ic_drawer_logout,
                        resTitle = R.string.drawer_item_title_logout,
                        onClick = {
                            Log.i(TAG, "action: logout user & relaunch screen navigation")
                        }
                )

                SeparatorView(
                        isSeparatorVisible = true,
                        topPadding = Space.MEDIUM,
                        bottomPadding = Space.MEDIUM
                )
            }

            IconTitleView(
                    resIcon = R.drawable.ic_drawer_about,
                    resTitle = R.string.drawer_item_title_about,
                    onClick = {
                        Log.i(TAG, "action: open 'about' screen")
                    }
            )

            IconTitleView(
                    resIcon = R.drawable.ic_privacy_link,
                    resTitle = R.string.drawer_item_title_privacy_policy,
                    onClick = {
                        Log.i(TAG, "action: open 'privacy policy' in 'web view' screen")
                    }
            )

            IconTitleView(
                    resIcon = R.drawable.ic_drawer_homepage,
                    resTitle = R.string.drawer_item_title_homepage,
                    onClick = {
                        Log.i(TAG, "action: open 'home page' in 'web view' screen")
                    }
            )

            IconTitleView(
                    resIcon = R.drawable.ic_drawer_support,
                    resTitle = R.string.drawer_item_title_contact_support,
                    onClick = {
                        Log.i(TAG, "action: open 'help desk' in 'web view' screen")
                    }
            )
        }
    }
}

@Composable
private fun IconAppVersionWithUsernameView() {
    val viewModel: SideMenuViewModel by viewModel()

    val appVersionFormat: String = stringResource(R.string.drawer_item_description_app_version_format)

    val title: String = viewModel.username
    val description: String = appVersionFormat.format(viewModel.versionName, viewModel.versionCode)

    val showUsername: Boolean = viewModel.username.isNotBlank()
    val showAppVersion: Boolean = viewModel.versionName.isNotBlank() || viewModel.versionCode.isNotBlank()

    Row(modifier = Modifier.fillMaxWidth()) {
        Image(
                painter = painterResource(id = R.drawable.drawer_icon),
                modifier = Modifier
                        .align(CenterVertically)
                        .padding(start = 24.dp, top = 10.dp, bottom = 10.dp)
                        .height(47.dp)
                        .width(47.dp),
                contentDescription = "icon"
        )

        Column(
                modifier = Modifier
                        .fillMaxWidth()
                        .align(CenterVertically)
                        .padding(start = 16.dp, top = 10.dp, bottom = 10.dp)
        ) {
            if (showUsername) {
                Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = title,
                        fontSize = SideMenuUiTheme.PiaTextTitle.textSize,
                        fontFamily = SideMenuUiTheme.PiaTextTitle.fontFamily,
                        style = SideMenuUiTheme.PiaTextTitle.textStyle.copy(
                                color = SideMenuUiTheme.colors.grey20_white
                        )
                )
            }

            if (showUsername && showAppVersion) {
                Spacer(modifier = Modifier.height(5.dp))
            }

            if (showAppVersion) {
                Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = description,
                        fontSize = 14.sp,
                        fontFamily = SideMenuUiTheme.PiaTextTitle.fontFamily,
                        style = SideMenuUiTheme.PiaTextTitle.textStyle.copy(
                                color = SideMenuUiTheme.colors.grey20_white
                        ),
                )
            }
        }
    }
}

@Composable
private fun IconTitleView(
        @DrawableRes resIcon: Int,
        @StringRes resTitle: Int,
        onClick: (() -> Unit)?
) {
    CompositionLocalProvider(
            values = arrayOf(LocalRippleTheme provides SideMenuRippleTheme)
    ) {

        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .makeClickable(
                                onClick = onClick,
                                hasRipple = true
                        )
        ) {
            Spacer(modifier = Modifier.width(24.dp))

            Image(
                    modifier = Modifier
                            .align(CenterVertically)
                            .padding(top = 6.dp, bottom = 6.dp)
                            .width(24.dp)
                            .height(24.dp),
                    painter = painterResource(id = resIcon),
                    contentScale = ContentScale.Inside,
                    contentDescription = "icon"
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                    text = stringResource(resTitle),
                    modifier = Modifier
                            .fillMaxWidth()
                            .align(CenterVertically),
                    fontSize = 15.sp,
                    fontFamily = SideMenuUiTheme.PiaTextBody2.fontFamily,
                    style = SideMenuUiTheme.PiaTextBody2.textStyle.copy(
                            color = SideMenuUiTheme.PiaTextBody2.textColor
                    ),
                    maxLines = 1
            )
        }
    }

}

@Composable
private fun SeparatorView(
        isSeparatorVisible: Boolean,
        topPadding: Dp,
        bottomPadding: Dp
) {
    if (isSeparatorVisible) {
        Divider(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = topPadding, bottom = bottomPadding)
                        .background(SideMenuUiTheme.colors.pia_sidemenu_divider)
        )
    } else {
        Spacer(
                modifier = Modifier
                        .fillMaxWidth()
                        .height(height = topPadding + bottomPadding)
        )
    }

}

@Composable
private fun SubscriptionExpiryView(
        onClick: (() -> Unit)?
) {
    val context: Context = LocalContext.current

    val title: String = getSubscriptionExpiryTitle()
    val description: String = stringResource(R.string.drawer_item_description_update_account)

    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .background(color = CommonSideMenuColors.connecting_orange)
                    .makeClickable(
                            onClick = onClick,
                            hasRipple = false
                    )
    ) {
        Spacer(modifier = Modifier.width(18.dp))

        Image(
                modifier = Modifier
                        .align(CenterVertically)
                        .padding(top = 6.dp, bottom = 6.dp)
                        .height(36.dp)
                        .width(36.dp),
                painter = painterResource(id = R.drawable.ic_orange_arrow_circle),
                contentDescription = "icon"
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column(
                modifier = Modifier
                        .align(CenterVertically)
                        .fillMaxWidth()
        ) {
            Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title.uppercase(context.resources.configuration.compatLocal),
                    fontSize = 12.sp,
                    fontFamily = SideMenuUiTheme.PiaExpirationTitle.fontFamily,
                    style = SideMenuUiTheme.PiaExpirationTitle.textStyle.copy(
                            color = SideMenuUiTheme.PiaExpirationTitle.textColor
                    ),
                    maxLines = 1
            )
            Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = description,
                    fontSize = 14.sp,
                    fontFamily = SideMenuUiTheme.PiaExpirationDescription.fontFamily,
                    style = SideMenuUiTheme.PiaExpirationDescription.textStyle.copy(
                            color = SideMenuUiTheme.PiaExpirationDescription.textColor
                    ),
                    maxLines = 1
            )
        }
    }
}

@Composable
private fun getSubscriptionExpiryTitle(): String {
    val viewModel: SideMenuViewModel by viewModel()

    val now: LocalDateTime = LocalDateTime.now()
    val subscriptionExpiryTime: LocalDateTime = viewModel.subscriptionExpiryTime

    if (subscriptionExpiryTime.isAfter(now).not()) {
        return stringResource(R.string.drawer_item_title_subscription_expired)
    }

    val timeLeft: Duration = Duration.between(now, subscriptionExpiryTime)
    return stringResource(R.string.drawer_item_title_subscription_expires_format).format(getTimeLeftText(timeLeft = timeLeft))
}

@Composable
private fun getTimeLeftText(
        timeLeft: Duration
): String {
    val viewModel: SideMenuViewModel by viewModel()

    val days: Long = timeLeft.toDays()
    val hours: Int = viewModel.compatToHoursPart(duration = timeLeft)
    val minutes: Int = viewModel.compatToMinutesPart(duration = timeLeft)
    return when {
        days == 1L -> stringResource(R.string.duration_one_day)
        days in 2L..Long.MAX_VALUE -> stringResource(R.string.duration_x_days).format(Locale.ENGLISH, days)
        hours == 1 -> stringResource(R.string.duration_one_hour)
        hours in 2..Int.MAX_VALUE -> stringResource(R.string.duration_x_hours).format(Locale.ENGLISH, hours)
        minutes == 1 -> stringResource(R.string.duration_one_minute)
        minutes in 2..Int.MAX_VALUE -> stringResource(R.string.duration_x_minutes).format(Locale.ENGLISH, minutes)
        else -> ""
    }
}

private fun Modifier.makeClickable(
        onClick: (() -> Unit)?,
        hasRipple: Boolean
): Modifier = composed {
    return@composed when {
        onClick != null -> clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = if (hasRipple) rememberRipple(bounded = true) else null,
                onClick = onClick@{
                    onClick()
                }
        )
        else -> this
    }
}

private val Configuration.compatLocal: Locale
    get() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> locales.get(0)
        else -> @Suppress(names = ["DEPRECATION"]) locale
    }

private class SideMenuUiDrawerScopeImpl(
        private val scope: CoroutineScope,
        private val drawerState: DrawerState
) : SideMenuUiDrawerScope {
    override fun openDrawer() {
        scope.launch {
            drawerState.open()
        }
    }

    override fun closeDrawer() {
        scope.launch {
            drawerState.close()
        }
    }
}

private class DrawerShape(
        private val maxSizePx: Float
) : Shape {
    override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density
    ): Outline {
        return Outline.Rectangle(
                Rect(
                        left = 0f,
                        top = 0f,
                        right = min(maxSizePx, size.width),
                        bottom = size.height
                )
        )
    }
}
//</editor-fold>
