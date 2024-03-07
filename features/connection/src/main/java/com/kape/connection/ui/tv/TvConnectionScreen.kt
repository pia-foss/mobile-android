package com.kape.connection.ui.tv

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.TabRowDefaults
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.sidemenu.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.theme.statusBarConnected
import com.kape.ui.theme.statusBarConnecting
import com.kape.ui.theme.statusBarDefault
import com.kape.ui.theme.statusBarError
import com.kape.ui.tv.elements.RoundIconButton
import com.kape.ui.tv.text.AppBarTitleText
import com.kape.ui.tv.text.PrimaryButtonText
import com.kape.ui.tv.text.SecondaryButtonText
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.Locale

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvConnectionScreen() = Screen {
    val viewModel: ConnectionViewModel = koinViewModel()
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val locale = Locale.getDefault().language

    BackHandler {
        viewModel.exitApp()
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.loadVpnServers(locale)
        viewModel.loadShadowsocksServers(locale)
        viewModel.autoConnect()
    }

    val tabs = listOf(
        stringResource(id = com.kape.ui.R.string.app_name),
        stringResource(com.kape.ui.R.string.location),
    )
    val selectedTabIndex = remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .background(LocalColors.current.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(0.25f),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    TabRow(
                        modifier = Modifier
                            .background(
                                color = LocalColors.current.primaryContainer,
                                shape = CircleShape,
                            )
                            .padding(4.dp),
                        selectedTabIndex = selectedTabIndex.intValue,
                        indicator = { tabPositions, _ ->
                            tabs.forEachIndexed { index, _ ->
                                TabRowDefaults.PillIndicator(
                                    currentTabPosition = tabPositions[index],
                                    doesTabRowHaveFocus = index == selectedTabIndex.intValue,
                                    activeColor = LocalColors.current.primary,
                                    inactiveColor = LocalColors.current.onPrimaryContainer,
                                )
                            }
                        },
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                                selected = index == selectedTabIndex.intValue,
                                onFocus = {
                                    selectedTabIndex.intValue = index
                                },
                            ) {
                                if (index == selectedTabIndex.intValue) {
                                    PrimaryButtonText(content = tab)
                                } else {
                                    SecondaryButtonText(content = tab)
                                }
                            }
                        }
                    }
                }
                AppBarTitleText(
                    content = "Not Connected",
                    textColor = getTopTextConnectionColor(
                        status = connectionStatus.value,
                        scheme = LocalColors.current,
                    ),
                    isError = false,
                    modifier = Modifier.weight(0.5f),
                )
                Row(
                    modifier = Modifier.weight(0.25f),
                    horizontalArrangement = Arrangement.End,
                ) {
                    RoundIconButton(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        painterId = R.drawable.ic_settings,
                    ) {
                        // Route to settings
                    }
                    RoundIconButton(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        painterId = R.drawable.ic_help,
                    ) {
                        // Route to help
                    }
                }
            }
            Row(modifier = Modifier.fillMaxSize()) {
                // To be done
            }
        }
    }
}

@Composable
private fun getTopBarConnectionColor(status: ConnectionStatus, scheme: ColorScheme): Color {
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
private fun getTopTextConnectionColor(status: ConnectionStatus, scheme: ColorScheme): Color {
    return when (status) {
        ConnectionStatus.ERROR -> scheme.statusBarError()
        ConnectionStatus.CONNECTED -> scheme.statusBarConnected()
        ConnectionStatus.DISCONNECTED -> LocalColors.current.onSurface
        ConnectionStatus.RECONNECTING,
        ConnectionStatus.CONNECTING,
        -> scheme.statusBarConnecting()
    }
}