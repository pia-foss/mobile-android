@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.kape.appbar.view.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.TabRowDefaults
import com.kape.ui.R
import com.kape.ui.theme.statusBarConnected
import com.kape.ui.theme.statusBarConnecting
import com.kape.ui.theme.statusBarError
import com.kape.ui.tv.elements.RoundIconButton
import com.kape.ui.tv.text.AppBarTitleText
import com.kape.ui.tv.text.PrimaryButtonText
import com.kape.ui.tv.text.SecondaryButtonText
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionStatus

@Composable
fun TvHomeHeaderItem(
    title: String? = null,
    connectionStatus: State<ConnectionStatus>,
) {
    val tabs = listOf(
        stringResource(id = R.string.app_name),
        stringResource(R.string.location),
    )
    val colorScheme = LocalColors.current
    val selectedTabIndex = remember { mutableIntStateOf(0) }
    val tabPillActiveColor = remember { mutableStateOf(colorScheme.primary) }
    val tabPillActiveTextColor = remember { mutableStateOf(colorScheme.onPrimary) }
    val tabPillInactiveColor = remember { mutableStateOf(colorScheme.onPrimaryContainer) }
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
                    .onFocusChanged {
                        if (it.hasFocus) {
                            tabPillActiveColor.value = colorScheme.primary
                            tabPillActiveTextColor.value = colorScheme.onPrimary
                            tabPillInactiveColor.value = colorScheme.onPrimaryContainer
                        } else {
                            tabPillActiveColor.value = colorScheme.onPrimaryContainer
                            tabPillActiveTextColor.value = colorScheme.onSurface
                            tabPillInactiveColor.value = colorScheme.primaryContainer
                        }
                    }
                    .padding(4.dp),
                selectedTabIndex = selectedTabIndex.intValue,
                indicator = { tabPositions, _ ->
                    tabs.forEachIndexed { index, _ ->
                        TabRowDefaults.PillIndicator(
                            currentTabPosition = tabPositions[index],
                            doesTabRowHaveFocus = index == selectedTabIndex.intValue,
                            activeColor = tabPillActiveColor.value,
                            inactiveColor = tabPillInactiveColor.value,
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
                            PrimaryButtonText(content = tab, color = tabPillActiveTextColor.value)
                        } else {
                            SecondaryButtonText(content = tab)
                        }
                    }
                }
            }
        }
        AppBarTitleText(
            content = title ?: getTopTextConnectionString(
                status = connectionStatus.value,
            ),
            textColor = title?.let {
                LocalColors.current.onSurface
            } ?: getTopTextConnectionColor(
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

@Composable
private fun getTopTextConnectionString(status: ConnectionStatus): String {
    return when (status) {
        ConnectionStatus.ERROR -> stringResource(id = com.kape.ui.R.string.connection_error)
        ConnectionStatus.CONNECTED -> stringResource(id = com.kape.ui.R.string.connected)
        ConnectionStatus.DISCONNECTED -> stringResource(id = com.kape.ui.R.string.not_connected)
        ConnectionStatus.RECONNECTING,
        ConnectionStatus.CONNECTING,
        -> stringResource(id = com.kape.ui.R.string.connecting)
    }
}