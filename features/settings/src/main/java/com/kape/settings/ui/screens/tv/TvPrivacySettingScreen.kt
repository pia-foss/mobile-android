package com.kape.settings.ui.screens.tv

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.settings.data.DnsOptions
import com.kape.settings.ui.elements.tv.TvSettingsToggle
import com.kape.settings.ui.screens.mobile.WarningDialog
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.tv.text.AppBarTitleText
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnregionselection.ui.tv.getTopBarConnectionColor
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun TvPrivacySettingsScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val initialFocusRequester = FocusRequester()
    val showWarning = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        initialFocusRequester.requestFocus()
    }

    BackHandler {
        viewModel.navigateUp()
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
                    content = stringResource(id = R.string.privacy),
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
                    TvSettingsToggle(
                        modifier = Modifier.focusRequester(initialFocusRequester),
                        titleId = R.string.mace_title,
                        subtitleId = R.string.mace_description,
                        enabled = viewModel.maceEnabled.value,
                        toggle = {
                            // MACE requires using PIA DNS
                            if (viewModel.getSelectedDnsOption() != DnsOptions.PIA && !viewModel.maceEnabled.value) {
                                showWarning.value = true
                            }
                            viewModel.toggleMace(it)
                            viewModel.showReconnectDialogIfVpnConnected()
                        },
                    )
                }
                Column(
                    modifier = Modifier.weight(1.0f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = com.kape.settings.R.drawable.ic_tv_settings),
                        contentScale = ContentScale.Fit,
                        contentDescription = null,
                    )
                }
            }
        }
    }
    if (showWarning.value) {
        WarningDialog(
            onConfirm = {
                viewModel.setSelectedDnsOption(DnsOptions.PIA)
                showWarning.value = false
            },
        )
    }
}