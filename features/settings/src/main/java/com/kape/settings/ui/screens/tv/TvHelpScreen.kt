package com.kape.settings.ui.screens.tv

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.settings.ui.elements.tv.TvSettingsItem
import com.kape.settings.ui.elements.tv.TvSettingsToggle
import com.kape.settings.ui.screens.mobile.SuccessDialog
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
    val showDialog = remember { mutableStateOf(false) }
    val showToast = remember { mutableStateOf(false) }
    val showSpinner = remember { mutableStateOf(false) }

    with(viewModel.requestId.value) {
        when {
            this == null -> {
                showDialog.value = false
                showToast.value = false
            }

            this.isEmpty() -> {
                showToast.value = true
            }

            this.isNotEmpty() -> {
                showDialog.value = true
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        initialFocusRequester.requestFocus()
    }

    BackHandler {
        viewModel.navigateToConnection()
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
                    if (showSpinner.value) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    } else {
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
                        Spacer(modifier = Modifier.height(8.dp))
                        TvSettingsToggle(
                            titleId = R.string.help_improve_pia_title,
                            subtitleId = R.string.help_improve_pia_description,
                            enabled = viewModel.improvePiaEnabled.value,
                            toggle = {
                                viewModel.toggleImprovePia(it)
                            },
                        )
                        if (viewModel.improvePiaEnabled.value) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TvSettingsItem(
                                titleId = R.string.help_view_shared_data_title,
                            ) {
                                viewModel.navigateToConnectionStats()
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TvSettingsItem(
                            titleId = R.string.about,
                        ) {
                            viewModel.navigateToAbout()
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TvSettingsItem(
                            titleId = R.string.drawer_item_title_privacy_policy,
                        ) {
                            viewModel.navigateToPrivacyPolicy()
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TvSettingsItem(
                            titleId = R.string.help_send_log_title,
                        ) {
                            showSpinner.value = true
                            viewModel.sendLogs()
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1.0f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(64.dp),
                        painter = painterResource(id = com.kape.settings.R.drawable.tv_help),
                        contentScale = ContentScale.Fit,
                        contentDescription = null,
                    )
                }
            }
        }

        if (showDialog.value) {
            showSpinner.value = false
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.6f),
            ) {
                SuccessDialog(
                    requestId = viewModel.requestId.value ?: "",
                    showDialog = showDialog,
                ) {
                    viewModel.resetRequestId()
                }
            }
        }

        if (showToast.value) {
            Toast.makeText(context, R.string.failure_sending_log, Toast.LENGTH_LONG).show()
            viewModel.resetRequestId()
            showSpinner.value = false
            showToast.value = false
        }
    }
}