package com.kape.profile.ui.tv

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import com.kape.appbar.view.topBarColor
import com.kape.contracts.ConnectionStatusSource
import com.kape.profile.ui.formatDate
import com.kape.profile.ui.vm.ProfileViewModel
import com.kape.ui.R
import com.kape.ui.Screen
import com.kape.ui.tiles.LogoutDialog
import com.kape.ui.tv.elements.PrimaryButton
import com.kape.ui.tv.text.AppBarTitleText
import com.kape.ui.tv.text.SettingsL2Text
import com.kape.ui.tv.text.SettingsL2TextDescription
import com.kape.ui.utils.LocalColors
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TvProfileScreen(viewModel: ProfileViewModel = koinViewModel()) =
    Screen {
        val connectionStatus by koinInject<ConnectionStatusSource>().status.collectAsStateWithLifecycle()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val logoutDialogVisible = remember { mutableStateOf(false) }

        Box(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 4.dp,
                color = connectionStatus.topBarColor(LocalColors.current),
            )
            Column(
                modifier =
                    Modifier
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
                        content = stringResource(id = R.string.account),
                        textColor = LocalColors.current.onSurface,
                        isError = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .weight(1.0f)
                                .padding(end = 64.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top,
                    ) {
                        if (state.isLoading) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        } else {
                            TvProfileItem(
                                title = stringResource(id = R.string.username),
                                subtitle = state.username,
                                onClick = { },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TvProfileItem(
                                title = stringResource(id = R.string.message_expiration),
                                subtitle =
                                    if (state.isExpired) {
                                        stringResource(id = R.string.subscription_status_expired)
                                    } else {
                                        state.expiresAtMillis?.let { formatDate(it) }.orEmpty()
                                    },
                                onClick = { },
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryButton(
                                text = stringResource(id = R.string.drawer_item_title_logout),
                                textAlign = TextAlign.Start,
                            ) {
                                logoutDialogVisible.value = true
                            }
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

            if (logoutDialogVisible.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.6f),
                ) {
                    LogoutDialog(
                        onDismiss = {
                            logoutDialogVisible.value = false
                        },
                        onConfirm = {
                            viewModel.logout()
                            logoutDialogVisible.value = false
                        },
                    )
                }
            }
        }
    }

@Composable
fun TvProfileItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    onClick: (() -> Unit),
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        shape =
            ButtonDefaults.shape(
                shape = RoundedCornerShape(12.dp),
            ),
        colors =
            ButtonDefaults.colors(
                containerColor = LocalColors.current.background,
                contentColor = LocalColors.current.onSurfaceVariant,
                focusedContainerColor = LocalColors.current.primary,
                focusedContentColor = LocalColors.current.onPrimary,
            ),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SettingsL2Text(content = title)
            subtitle?.let {
                SettingsL2TextDescription(content = it)
            }
        }
    }
}