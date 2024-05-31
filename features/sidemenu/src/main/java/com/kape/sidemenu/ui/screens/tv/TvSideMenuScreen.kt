package com.kape.sidemenu.ui.screens.tv

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kape.sidemenu.ui.vm.SideMenuViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.theme.statusBarConnected
import com.kape.ui.theme.statusBarConnecting
import com.kape.ui.theme.statusBarDefault
import com.kape.ui.theme.statusBarError
import com.kape.ui.tv.elements.SecondaryButton
import com.kape.ui.tv.text.AppBarTitleText
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun TvSideMenuScreen() = Screen {
    val viewModel: SideMenuViewModel = koinViewModel()
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val initialFocusRequester = FocusRequester()

    LaunchedEffect(key1 = Unit) {
        initialFocusRequester.requestFocus()
    }

    BackHandler {
        viewModel.navigateToVpnConnect()
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 4.dp,
            color = getTopBarConnectionColor(
                status = connectionStatus.value,
                scheme = LocalColors.current,
            ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, top = 24.dp, end = 32.dp, bottom = 0.dp)
                .background(LocalColors.current.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppBarTitleText(
                    content = stringResource(id = R.string.settings),
                    textColor = LocalColors.current.onSurface,
                    isError = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(end = 64.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                ) {
                    SecondaryButton(
                        text = stringResource(id = R.string.account),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.focusRequester(initialFocusRequester),
                    ) {
                        viewModel.navigateToProfile()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SecondaryButton(
                        text = stringResource(id = R.string.general_settings),
                        textAlign = TextAlign.Start,
                    ) {
                        viewModel.navigateToSettings()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SecondaryButton(
                        text = stringResource(id = R.string.dedicated_ip),
                        textAlign = TextAlign.Start,
                    ) {
                        viewModel.navigateToDedicatedIp()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SecondaryButton(
                        text = stringResource(id = R.string.per_app_settings),
                        textAlign = TextAlign.Start,
                    ) {
                        viewModel.navigateToPerAppSettings()
                    }
                }
                Column(
                    modifier = Modifier.weight(1.0f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_tv_settings),
                        contentScale = ContentScale.Fit,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
private fun getTopBarConnectionColor(status: ConnectionStatus, scheme: ColorScheme): Color {
    return when (status) {
        ConnectionStatus.ERROR -> scheme.statusBarError()
        ConnectionStatus.CONNECTED -> scheme.statusBarConnected()
        ConnectionStatus.DISCONNECTED, ConnectionStatus.DISCONNECTING -> scheme.statusBarDefault(scheme)
        ConnectionStatus.RECONNECTING, ConnectionStatus.CONNECTING -> scheme.statusBarConnecting()
    }
}