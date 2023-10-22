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
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.ui.text.AppBarConnectionTextDefault
import com.kape.ui.text.AppBarTitleText
import com.kape.ui.theme.connectedGradient
import com.kape.ui.theme.connectingGradient
import com.kape.ui.theme.defaultGradient
import com.kape.ui.theme.statusBarConnected
import com.kape.ui.theme.statusBarConnecting
import com.kape.ui.theme.statusBarDefault
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionStatus

@Composable
fun IAppBar(
    viewModel: AppBarViewModel,
    type: AppBarType = AppBarType.Navigation,
    onLeftIconClick: () -> Unit,
) {
    val scheme = LocalColors.current
    val showDarkIcons = shouldShowDarkIcons(connectionState = viewModel.appBarConnectionState)

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            getStatusBarColor(
                viewModel.appBarConnectionState,
                scheme,
            ),
            showDarkIcons,
        )
    }

    AppBarContent(
        type = type,
        status = viewModel.appBarConnectionState,
        viewModel.appBarText,
        onLeftIconClick,
    )
}

@Composable
private fun AppBarContent(
    type: AppBarType,
    status: ConnectionStatus,
    title: String? = null,
    onLeftIconClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(getAppBarBackgroundColor(status, LocalColors.current)),
    ) {
        IconButton(
            onClick = { onLeftIconClick() },
            modifier = Modifier
                .align(CenterStart),
        ) {
            Icon(painter = painterResource(id = getAppBarLeftIcon(type)), contentDescription = null)
        }

        when (type) {
            AppBarType.Connection -> {
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
            }

            AppBarType.InAppBrowser -> {
                Icon(
                    painter = painterResource(id = com.kape.ui.R.drawable.ic_logo_medium),
                    contentDescription = null,
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
        ConnectionStatus.CONNECTED -> Brush.verticalGradient(scheme.connectedGradient())
        ConnectionStatus.DISCONNECTED -> Brush.verticalGradient(scheme.defaultGradient())
        ConnectionStatus.CONNECTING,
        ConnectionStatus.RECONNECTING,
        -> Brush.verticalGradient(scheme.connectingGradient())
    }
}

private fun getStatusBarColor(status: ConnectionStatus, scheme: ColorScheme): Color {
    return when (status) {
        ConnectionStatus.CONNECTED -> scheme.statusBarConnected()
        ConnectionStatus.DISCONNECTED -> scheme.statusBarDefault()
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
        -> true
    }
}

sealed class AppBarType {
    data object Connection : AppBarType()
    data object Navigation : AppBarType()
    data object InAppBrowser : AppBarType()
}