package com.kape.appbar.view

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.ui.text.AppBarConnectionTextDefault
import com.kape.ui.text.AppBarTitleText
import com.kape.ui.theme.connectedGradient
import com.kape.ui.theme.connectingGradient
import com.kape.ui.theme.defaultGradient
import com.kape.ui.theme.errorGradient
import com.kape.ui.theme.statusBarConnected
import com.kape.ui.theme.statusBarConnecting
import com.kape.ui.theme.statusBarDefault
import com.kape.ui.theme.statusBarError
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.connectivityState
import com.kape.utils.InternetConnectionState
import com.kape.vpnconnect.utils.ConnectionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun AppBar(
    viewModel: AppBarViewModel,
    type: AppBarType = AppBarType.Navigation,
    onLeftIconClick: () -> Unit,
    onRightIconClick: () -> Unit = {},
) {
    val scheme = LocalColors.current
    val showDarkIcons = shouldShowDarkIcons(connectionState = viewModel.appBarConnectionState)
    val connection by connectivityState()
    val isConnected = connection === InternetConnectionState.Connected

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            getStatusBarColor(
                if (isConnected) viewModel.appBarConnectionState else ConnectionStatus.ERROR,
                scheme,
            ),
            showDarkIcons,
        )
    }

    AppBarContent(
        type = type,
        status = if (isConnected) viewModel.appBarConnectionState else ConnectionStatus.ERROR,
        if (isConnected) viewModel.appBarText else stringResource(id = com.kape.ui.R.string.no_internet_connection),
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
        IconButton(
            onClick = { onLeftIconClick() },
            modifier = Modifier
                .align(CenterStart)
                .testTag(":AppBar:side_menu"),
        ) {
            Icon(painter = painterResource(id = getAppBarLeftIcon(type)), contentDescription = null)
        }

        when (type) {
            AppBarType.Connection -> {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.align(Center),
                    ) {
                        if (status == ConnectionStatus.DISCONNECTED) {
                            Icon(
                                painter = painterResource(id = com.kape.ui.R.drawable.ic_logo_small),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(CenterVertically)
                                    .padding(end = 8.dp),
                                tint = Color.Unspecified,
                            )
                        }

                        AppBarConnectionTextDefault(
                            content = title ?: "",
                            modifier = Modifier.align(CenterVertically),
                        )
                    }

                    IconButton(
                        onClick = { onRightIconClick() },
                        modifier = Modifier.align(CenterEnd),
                    ) {
                        Icon(
                            painter = painterResource(id = com.kape.ui.R.drawable.ic_reorder),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            AppBarType.InAppBrowser -> {
                Icon(
                    painter = painterResource(id = com.kape.ui.R.drawable.ic_logo_medium),
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
        AppBarType.Connection -> com.kape.ui.R.drawable.ic_menu
        AppBarType.InAppBrowser,
        AppBarType.Navigation,
        -> com.kape.ui.R.drawable.ic_back
    }
}

private fun getAppBarBackgroundColor(status: ConnectionStatus, scheme: ColorScheme): Brush {
    return when (status) {
        ConnectionStatus.ERROR -> Brush.verticalGradient(scheme.errorGradient())
        ConnectionStatus.CONNECTED -> Brush.verticalGradient(scheme.connectedGradient())
        ConnectionStatus.DISCONNECTED -> Brush.verticalGradient(scheme.defaultGradient(scheme))
        ConnectionStatus.CONNECTING,
        ConnectionStatus.RECONNECTING,
        -> Brush.verticalGradient(scheme.connectingGradient())
    }
}

private fun getStatusBarColor(status: ConnectionStatus, scheme: ColorScheme): Color {
    return when (status) {
        ConnectionStatus.ERROR -> scheme.statusBarError()
        ConnectionStatus.CONNECTED -> scheme.statusBarConnected()
        ConnectionStatus.DISCONNECTED -> scheme.statusBarDefault(scheme)
        ConnectionStatus.RECONNECTING,
        ConnectionStatus.CONNECTING,
        -> scheme.statusBarConnecting()
    }
}

@Composable
private fun shouldShowDarkIcons(connectionState: ConnectionStatus): Boolean {
    return when (connectionState) {
        ConnectionStatus.DISCONNECTED -> !isSystemInDarkTheme()
        ConnectionStatus.RECONNECTING,
        ConnectionStatus.CONNECTED,
        ConnectionStatus.CONNECTING,
        ConnectionStatus.ERROR,
        -> true
    }
}

sealed class AppBarType {
    data object Connection : AppBarType()
    data object Navigation : AppBarType()
    data object InAppBrowser : AppBarType()
}