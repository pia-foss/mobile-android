package com.kape.settings.ui.screens.tv

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.settings.ui.elements.tv.TvSettingsItem
import com.kape.settings.ui.elements.tv.TvSettingsToggle
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.tv.text.AppBarTitleText
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionManager
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun TvHelpScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val initialFocusRequester = FocusRequester()

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        initialFocusRequester.requestFocus()
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
                    content = stringResource(id = R.string.help),
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
                    val appUrl = stringResource(id = R.string.app_url)
                    TvSettingsItem(
                        modifier = Modifier.focusRequester(initialFocusRequester),
                        titleId = R.string.help_version_title,
                        subtitle = viewModel.version,
                    ) {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(appUrl)
                            },
                        )
                    }
                    TvSettingsToggle(
                        titleId = R.string.help_improve_pia_title,
                        subtitleId = R.string.help_improve_pia_description,
                        enabled = viewModel.improvePiaEnabled.value,
                        toggle = {
                            viewModel.toggleImprovePia(it)
                        },
                    )
                    if (viewModel.improvePiaEnabled.value) {
                        TvSettingsItem(
                            titleId = R.string.help_view_shared_data_title,
                        ) {
                            viewModel.navigateToConnectionStats()
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1.0f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize().padding(64.dp),
                        painter = painterResource(id = com.kape.settings.R.drawable.tv_help),
                        contentScale = ContentScale.Fit,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}