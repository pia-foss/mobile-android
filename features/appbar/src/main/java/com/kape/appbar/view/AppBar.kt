package com.kape.appbar.view

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.ui.R
import com.kape.ui.theme.PiaScreen
import com.kape.ui.theme.appbarConnectedGradient
import com.kape.ui.theme.appbarConnectedStatus
import com.kape.ui.theme.appbarConnectingGradient
import com.kape.ui.theme.appbarConnectingStatus
import com.kape.ui.utils.LocalColors
import com.kape.utils.ConnectionListener

@Composable
fun ConnectionAppBar(
    viewModel: AppBarViewModel,
    onHeaderClick: () -> Unit,
    onLeftButtonClick: () -> Unit,
    onRightButtonClick: () -> Unit = {}
) {
    AppBar(
        state = AppBarState(
            title = viewModel.appBarText,
            accessibilityPrefix = viewModel.accessibilityPrefix,
            connectionState = viewModel.appBarConnectionState,
            colorScheme = LocalColors.current,
            navigationState = NavigationState.Menu,
            contentAlignment = ContentAlignment.Centered,
            contentType = if (viewModel.appBarConnectionState == ConnectionListener.ConnectionStatus.DISCONNECTED) {
                ContentType.ImageText
            } else {
                ContentType.Text
            },
            darkIcons = shouldShowDarkIcons(viewModel.appBarConnectionState)
        ),
        onHeaderClick = onHeaderClick,
        onLeftButtonClick = onLeftButtonClick,
        onRightButtonClick = onRightButtonClick
    )
}

@Composable
fun NavigationAppBar(
    viewModel: AppBarViewModel,
    onLeftButtonClick: () -> Unit,
    onRightButtonClick: () -> Unit = {}
) {
    AppBar(
        state = AppBarState(
            title = viewModel.appBarText,
            connectionState = viewModel.appBarConnectionState,
            colorScheme = LocalColors.current,
            navigationState = NavigationState.Back,
            contentAlignment = ContentAlignment.LeftAligned,
            contentType = ContentType.Text,
            darkIcons = shouldShowDarkIcons(viewModel.appBarConnectionState)
        ),
        onLeftButtonClick = onLeftButtonClick,
        onRightButtonClick = onRightButtonClick
    )
}

@Composable
fun InAppBrowserAppBar(
    viewModel: AppBarViewModel,
    onLeftButtonClick: () -> Unit,
    onRightButtonClick: () -> Unit = {}
) {
    AppBar(
        state = AppBarState(
            title = viewModel.appBarText,
            connectionState = viewModel.appBarConnectionState,
            colorScheme = LocalColors.current,
            navigationState = NavigationState.Back,
            contentAlignment = ContentAlignment.Centered,
            contentType = ContentType.Image,
            darkIcons = shouldShowDarkIcons(viewModel.appBarConnectionState)
        ),
        onLeftButtonClick = onLeftButtonClick,
        onRightButtonClick = onRightButtonClick
    )
}

@Composable
private fun AppBar(
    state: AppBarState,
    onHeaderClick: (() -> Unit)? = null,
    onLeftButtonClick: () -> Unit,
    onRightButtonClick: () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(state.colors.statusBar, state.colors.darkIcons)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(state.colors.background)
    ) {
        val navigationIcon: ImageVector
        val accessibilityText: String

        when (state.navigationState) {
            NavigationState.Back -> {
                navigationIcon = Icons.Default.ArrowBack
                accessibilityText =
                    stringResource(id = R.string.back)
            }

            NavigationState.Menu -> {
                navigationIcon = Icons.Default.Menu
                accessibilityText =
                    stringResource(id = R.string.menu)
            }
        }

        IconButton(
            onClick = onLeftButtonClick,
            modifier = Modifier
                .align(CenterStart)
                .semantics(mergeDescendants = true) { contentDescription = accessibilityText }
        ) {
            Icon(
                navigationIcon,
                contentDescription = null,
                tint = state.colors.foreground
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        val contentAlignment = when (state.contentAlignment) {
            ContentAlignment.Centered -> Center
            ContentAlignment.LeftAligned -> CenterStart
        }

        val headerModifier = onHeaderClick?.let {
            Modifier
                .align(contentAlignment)
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null
                ) { it() }
        } ?: Modifier.align(contentAlignment)

        Row(modifier = headerModifier) {
            when (state.contentType) {
                ContentType.Image -> {
                    ImageContent(
                        connectionState = state.connectionState,
                        color = state.colors.foreground
                    )
                }

                ContentType.ImageText -> {
                    ImageTextContent(
                        prefix = state.accessibilityPrefix ?: "",
                        title = state.title,
                        color = state.colors.foreground
                    )
                }

                ContentType.Text -> {
                    TextContent(
                        needsSpacing = state.navigationState is NavigationState.Back,
                        prefix = state.accessibilityPrefix ?: "",
                        title = state.title,
                        color = state.colors.foreground
                    )
                }
            }
        }

//        if (state.navigationState is NavigationState.Menu && state.feedbackSupported) {
//            IconButton(
//                onClick = onRightButtonClick,
//                modifier = Modifier.align(Alignment.CenterEnd)
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.fluffer_menu_ic_bug),
//                    contentDescription = stringResource(id = R.string.appbar__pia_home_screen__report_bug_button),
//                    tint = state.colors.foreground
//                )
//            }
//        }
    }
}

@Composable
fun ImageContent(connectionState: ConnectionListener.ConnectionStatus, color: Color) {
    if (connectionState is ConnectionListener.ConnectionStatus.CONNECTED) {
        Icon(
            painter = painterResource(id = R.drawable.ic_pia_logo),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.height(48.dp)
        )
    } else {
        Icon(
            painter = painterResource(id = R.drawable.ic_pia_logo),
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}

@Composable
fun ImageTextContent(prefix: String, title: String, color: Color) {
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = prefix
        }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_pia_logo),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.width(80.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
    }
}

@Composable
fun TextContent(needsSpacing: Boolean, prefix: String, title: String, color: Color) {
    if (needsSpacing) {
        Spacer(modifier = Modifier.width(48.dp))
    }
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = color,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "$prefix $title"
        }
    )
}

private data class AppBarState(
    val title: String,
    val accessibilityPrefix: String? = null,
    val connectionState: ConnectionListener.ConnectionStatus,
    val colorScheme: ColorScheme,
    val darkIcons: Boolean,
    val colors: AppBarColors = getAppBarColors(colorScheme, connectionState, darkIcons),
    val navigationState: NavigationState,
    val contentType: ContentType,
    val contentAlignment: ContentAlignment
)

private sealed class NavigationState {
    data object Menu : NavigationState()
    data object Back : NavigationState()
}

private sealed class ContentAlignment {
    data object Centered : ContentAlignment()
    data object LeftAligned : ContentAlignment()
}

private sealed class ContentType {
    data object Text : ContentType()
    data object Image : ContentType()
    data object ImageText : ContentType()
}

private data class AppBarColors(
    val background: Brush,
    val foreground: Color,
    val statusBar: Color,
    val darkIcons: Boolean
)

@Composable
private fun shouldShowDarkIcons(connectionState: ConnectionListener.ConnectionStatus): Boolean {
    return when (connectionState) {
        ConnectionListener.ConnectionStatus.DISCONNECTED -> !isSystemInDarkTheme()
        ConnectionListener.ConnectionStatus.RECONNECTING,
        ConnectionListener.ConnectionStatus.CONNECTED,
        ConnectionListener.ConnectionStatus.CONNECTING -> true
    }
}

private fun getAppBarColors(
    scheme: ColorScheme,
    connectionState: ConnectionListener.ConnectionStatus,
    darkIcons: Boolean
): AppBarColors {
    return when (connectionState) {
        ConnectionListener.ConnectionStatus.CONNECTED -> AppBarColors(
            background = Brush.verticalGradient(appbarConnectedGradient),
            foreground = Color.Black,
            statusBar = appbarConnectedStatus,
            darkIcons = darkIcons
        )

        ConnectionListener.ConnectionStatus.CONNECTING, ConnectionListener.ConnectionStatus.RECONNECTING -> AppBarColors(
            background = Brush.verticalGradient(appbarConnectingGradient),
            foreground = Color.Black,
            statusBar = appbarConnectingStatus,
            darkIcons = darkIcons
        )

        ConnectionListener.ConnectionStatus.DISCONNECTED -> AppBarColors(
            background = Brush.verticalGradient(
                listOf(
                    scheme.surface,
                    scheme.surface
                )
            ),
            foreground = scheme.onSurface,
            statusBar = scheme.surface,
            darkIcons = darkIcons
        )
    }
}

@Preview(name = "PreviewAppBar - Default - Light Theme")
@Preview(name = "PreviewAppBar - Default - Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewDefaultState() {
    PiaScreen {
        AppBar(
            state = AppBarState(
                title = "Not Connected",
                connectionState = ConnectionListener.ConnectionStatus.DISCONNECTED,
                colorScheme = LocalColors.current,
                navigationState = NavigationState.Menu,
                contentAlignment = ContentAlignment.Centered,
                contentType = ContentType.ImageText,
                darkIcons = shouldShowDarkIcons(ConnectionListener.ConnectionStatus.DISCONNECTED)
            ),
            onLeftButtonClick = {},
            onRightButtonClick = {}
        )
    }
}

@Preview(name = "PreviewAppBar - Connecting - Light Theme")
@Preview(name = "PreviewAppBar - Connecting - Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewConnectingState() {
    PiaScreen {
        AppBar(
            state = AppBarState(
                title = "Connecting",
                connectionState = ConnectionListener.ConnectionStatus.CONNECTING,
                colorScheme = LocalColors.current,
                navigationState = NavigationState.Menu,
                contentAlignment = ContentAlignment.Centered,
                contentType = ContentType.Text,
                darkIcons = shouldShowDarkIcons(ConnectionListener.ConnectionStatus.CONNECTING)
            ),
            onLeftButtonClick = {},
            onRightButtonClick = {}
        )
    }
}

@Preview(name = "PreviewAppBar - Connected - Light Theme")
@Preview(name = "PreviewAppBar - Connected - Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewConnectedState() {
    PiaScreen {
        AppBar(
            state = AppBarState(
                title = "Connected",
                connectionState = ConnectionListener.ConnectionStatus.CONNECTED,
                colorScheme = LocalColors.current,
                navigationState = NavigationState.Menu,
                contentAlignment = ContentAlignment.Centered,
                contentType = ContentType.Text,
                darkIcons = shouldShowDarkIcons(ConnectionListener.ConnectionStatus.CONNECTED)
            ),
            onLeftButtonClick = {},
            onRightButtonClick = {}
        )
    }
}

@Preview(name = "PreviewAppBar - Disconnected - Light Theme")
@Preview(
    name = "PreviewAppBar - Disconnected - Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewDisconnectedState() {
    PiaScreen {
        AppBar(
            state = AppBarState(
                title = "Error",
                connectionState = ConnectionListener.ConnectionStatus.DISCONNECTED,
                colorScheme = LocalColors.current,
                navigationState = NavigationState.Menu,
                contentAlignment = ContentAlignment.Centered,
                contentType = ContentType.Text,
                darkIcons = shouldShowDarkIcons(ConnectionListener.ConnectionStatus.DISCONNECTED)
            ),
            onLeftButtonClick = {},
            onRightButtonClick = {}
        )
    }
}

@Preview(name = "PreviewNavigationAppBar - Default - Light Theme")
@Preview(
    name = "PreviewNavigationAppBar - Default - Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewNavigationDefaultState() {
    PiaScreen {
        AppBar(
            state = AppBarState(
                title = "Title",
                connectionState = ConnectionListener.ConnectionStatus.DISCONNECTED,
                colorScheme = LocalColors.current,
                navigationState = NavigationState.Back,
                contentAlignment = ContentAlignment.LeftAligned,
                contentType = ContentType.Text,
                darkIcons = shouldShowDarkIcons(ConnectionListener.ConnectionStatus.DISCONNECTED)
            ),
            onLeftButtonClick = {},
            onRightButtonClick = {}
        )
    }
}

@Preview(name = "PreviewNavigationAppBar - Connecting - Light Theme")
@Preview(
    name = "PreviewNavigationAppBar - Connecting - Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewNavigationConnectingState() {
    PiaScreen {
        AppBar(
            state = AppBarState(
                title = "Title",
                connectionState = ConnectionListener.ConnectionStatus.CONNECTING,
                colorScheme = LocalColors.current,
                navigationState = NavigationState.Back,
                contentAlignment = ContentAlignment.LeftAligned,
                contentType = ContentType.Text,
                darkIcons = shouldShowDarkIcons(ConnectionListener.ConnectionStatus.CONNECTING)
            ),
            onLeftButtonClick = {},
            onRightButtonClick = {}
        )
    }
}

@Preview(name = "PreviewNavigationAppBar - Connected - Light Theme")
@Preview(
    name = "PreviewNavigationAppBar - Connected - Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewNavigationConnectedState() {
    PiaScreen {
        AppBar(
            state = AppBarState(
                title = "Title",
                connectionState = ConnectionListener.ConnectionStatus.CONNECTED,
                colorScheme = LocalColors.current,
                navigationState = NavigationState.Back,
                contentAlignment = ContentAlignment.LeftAligned,
                contentType = ContentType.Text,
                darkIcons = shouldShowDarkIcons(ConnectionListener.ConnectionStatus.CONNECTED)
            ),
            onLeftButtonClick = {},
            onRightButtonClick = {}
        )
    }
}

@Preview(name = "PreviewNavigationAppBar - Disconnected - Light Theme")
@Preview(
    name = "PreviewNavigationAppBar - Disconnected - Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewNavigationDisconnectedState() {
    PiaScreen {
        AppBar(
            state = AppBarState(
                title = "Title",
                connectionState = ConnectionListener.ConnectionStatus.DISCONNECTED,
                colorScheme = LocalColors.current,
                navigationState = NavigationState.Back,
                contentAlignment = ContentAlignment.LeftAligned,
                contentType = ContentType.Text,
                darkIcons = shouldShowDarkIcons(ConnectionListener.ConnectionStatus.DISCONNECTED)
            ),
            onLeftButtonClick = {},
            onRightButtonClick = {}
        )
    }
}

@Preview(name = "PreviewInAppBrowser - Default - Light Theme")
@Preview(
    name = "PreviewInAppBrowser - Default - Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewInAppBrowserDefaultState() {
    PiaScreen {
        AppBar(
            state = AppBarState(
                title = "Title",
                connectionState = ConnectionListener.ConnectionStatus.DISCONNECTED,
                colorScheme = LocalColors.current,
                navigationState = NavigationState.Back,
                contentAlignment = ContentAlignment.Centered,
                contentType = ContentType.Image,
                darkIcons = shouldShowDarkIcons(ConnectionListener.ConnectionStatus.DISCONNECTED)
            ),
            onLeftButtonClick = {},
            onRightButtonClick = {}
        )
    }
}

@Preview(name = "PreviewInAppBrowser - Connecting - Light Theme")
@Preview(
    name = "PreviewInAppBrowser - Connecting - Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewInAppBrowserConnectingState() {
    PiaScreen {
        AppBar(
            state = AppBarState(
                title = "Title",
                connectionState = ConnectionListener.ConnectionStatus.CONNECTING,
                colorScheme = LocalColors.current,
                navigationState = NavigationState.Back,
                contentAlignment = ContentAlignment.Centered,
                contentType = ContentType.Image,
                darkIcons = shouldShowDarkIcons(ConnectionListener.ConnectionStatus.CONNECTING)
            ),
            onLeftButtonClick = {},
            onRightButtonClick = {}
        )
    }
}

@Preview(name = "PreviewInAppBrowser - Disconnected - Light Theme")
@Preview(
    name = "PreviewInAppBrowser - Disconnected - Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewInAppBrowserDisconnectedState() {
    PiaScreen {
        AppBar(
            state = AppBarState(
                title = "Title",
                connectionState = ConnectionListener.ConnectionStatus.DISCONNECTED,
                colorScheme = LocalColors.current,
                navigationState = NavigationState.Back,
                contentAlignment = ContentAlignment.Centered,
                contentType = ContentType.Image,
                darkIcons = shouldShowDarkIcons(ConnectionListener.ConnectionStatus.DISCONNECTED)
            ),
            onLeftButtonClick = {},
            onRightButtonClick = {}
        )
    }
}