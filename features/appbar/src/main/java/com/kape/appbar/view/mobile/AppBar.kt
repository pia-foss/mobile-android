package com.kape.appbar.view.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.ui.R
import com.kape.ui.mobile.text.AppBarConnectionTextDefault
import com.kape.ui.mobile.text.AppBarTitleText
import com.kape.ui.theme.connectedGradient
import com.kape.ui.theme.connectingGradient
import com.kape.ui.theme.defaultGradient
import com.kape.ui.theme.errorGradient
import com.kape.ui.theme.statusBarConnected
import com.kape.ui.theme.statusBarConnecting
import com.kape.ui.theme.statusBarDefault
import com.kape.ui.theme.statusBarError
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionStatus

@Composable
fun AppBar(
    viewModel: AppBarViewModel,
    type: AppBarType = AppBarType.Navigation,
    onLeftIconClick: () -> Unit,
    onRightIconClick: () -> Unit = {},
) {
    val scheme = LocalColors.current
    val systemUiController = rememberSystemUiController()
    val isConnected = viewModel.isConnected.collectAsState()

    SideEffect {
        systemUiController.setStatusBarColor(
            getStatusBarColor(
                if (isConnected.value) viewModel.appBarConnectionState else ConnectionStatus.ERROR,
                scheme,
            ),
            shouldShowDarkIcons(connectionState = if (isConnected.value) viewModel.appBarConnectionState else ConnectionStatus.ERROR),
        )
    }

    AppBarContent(
        type = type,
        status = if (isConnected.value) viewModel.appBarConnectionState else ConnectionStatus.ERROR,
        if (isConnected.value) viewModel.appBarText else stringResource(id = R.string.no_internet_connection),
        onLeftIconClick,
        onRightIconClick,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AppBarContent(
    type: AppBarType,
    status: ConnectionStatus,
    title: String? = null,
    onLeftIconClick: () -> Unit,
    onRightIconClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(getAppBarBackgroundColor(status, LocalColors.current))
            .semantics {
                testTagsAsResourceId = true
            },

    ) {
        val menuContentDescription = stringResource(id = R.string.menu)
        val backContentDescription = stringResource(id = R.string.back)
        IconButton(
            onClick = { onLeftIconClick() },
            modifier = Modifier
                .align(CenterStart)
                .testTag(":AppBar:side_menu")
                .semantics {
                    contentDescription = when (type) {
                        AppBarType.Connection -> menuContentDescription
                        AppBarType.Customization,
                        AppBarType.InAppBrowser,
                        AppBarType.Navigation,
                        -> backContentDescription
                    }
                },
        ) {
            Icon(
                painter = painterResource(id = getAppBarLeftIcon(type)),
                contentDescription = null,
                tint = if (status == ConnectionStatus.ERROR) LocalColors.current.onPrimary else Color.Unspecified,
            )
        }

        when (type) {
            AppBarType.Connection -> {
                AppBarConnectionStatus(
                    status = status,
                    title = title,
                    onRightIconClick = onRightIconClick,
                    R.drawable.ic_reorder,
                )
            }

            AppBarType.Customization -> {
                AppBarConnectionStatus(
                    status = status,
                    title = title,
                    onRightIconClick = onRightIconClick,
                    R.drawable.ic_save,
                )
            }

            AppBarType.InAppBrowser -> {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo_medium),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .align(Center)
                        .fillMaxWidth(),
                )
            }

            AppBarType.Navigation -> {
                AppBarTitleText(
                    content = title ?: "",
                    isError = status == ConnectionStatus.ERROR,
                    modifier = Modifier
                        .padding(start = 56.dp)
                        .align(Center)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

private fun getAppBarLeftIcon(type: AppBarType): Int {
    return when (type) {
        AppBarType.Connection -> R.drawable.ic_menu
        AppBarType.Customization,
        AppBarType.InAppBrowser,
        AppBarType.Navigation,
        -> R.drawable.ic_back
    }
}

private fun getAppBarBackgroundColor(status: ConnectionStatus, scheme: ColorScheme): Brush {
    return when (status) {
        ConnectionStatus.ERROR -> Brush.verticalGradient(scheme.errorGradient())
        ConnectionStatus.CONNECTED -> Brush.verticalGradient(scheme.connectedGradient())
        ConnectionStatus.DISCONNECTING,
        ConnectionStatus.DISCONNECTED,
        -> Brush.verticalGradient(scheme.defaultGradient(scheme))

        ConnectionStatus.CONNECTING,
        ConnectionStatus.RECONNECTING,
        -> Brush.verticalGradient(scheme.connectingGradient())
    }
}

private fun getStatusBarColor(status: ConnectionStatus, scheme: ColorScheme): Color {
    return when (status) {
        ConnectionStatus.ERROR -> scheme.statusBarError()
        ConnectionStatus.CONNECTED -> scheme.statusBarConnected()
        ConnectionStatus.DISCONNECTED, ConnectionStatus.DISCONNECTING -> scheme.statusBarDefault(
            scheme,
        )

        ConnectionStatus.RECONNECTING,
        ConnectionStatus.CONNECTING,
        -> scheme.statusBarConnecting()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AppBarConnectionStatus(
    status: ConnectionStatus,
    title: String? = null,
    onRightIconClick: (() -> Unit)? = null,
    rightIconId: Int? = null,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.align(Center),
        ) {
            if (status == ConnectionStatus.DISCONNECTED) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo_small),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .align(CenterVertically)
                        .padding(end = 8.dp),
                    tint = Color.Unspecified,
                )
            }
            val statusPrefix = stringResource(id = R.string.vpn_status)
            AppBarConnectionTextDefault(
                content = title ?: "",
                isError = (status == ConnectionStatus.ERROR),
                modifier = Modifier
                    .align(CenterVertically)
                    .semantics {
                        contentDescription = "$statusPrefix $status"
                    },
            )
        }

        onRightIconClick?.let {
            IconButton(
                onClick = { it() },
                modifier = Modifier
                    .align(CenterEnd)
                    .semantics {
                        invisibleToUser()
                    },
            ) {
                rightIconId?.let {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = null,
                        tint = if (status == ConnectionStatus.ERROR) LocalColors.current.onPrimary else Color.Unspecified,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

private fun shouldShowDarkIcons(connectionState: ConnectionStatus): Boolean {
    return when (connectionState) {
        ConnectionStatus.DISCONNECTED,
        ConnectionStatus.DISCONNECTING,
        ConnectionStatus.RECONNECTING,
        ConnectionStatus.CONNECTED,
        ConnectionStatus.CONNECTING,
        -> true

        ConnectionStatus.ERROR,
        -> false
    }
}

sealed class AppBarType {
    data object Connection : AppBarType()
    data object Navigation : AppBarType()
    data object InAppBrowser : AppBarType()
    data object Customization : AppBarType()
}