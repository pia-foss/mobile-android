@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.kape.settings.ui.screens.tv

import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.kape.settings.ui.elements.ReconnectDialog
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.elements.Search
import com.kape.ui.tv.text.AppBarTitleText
import com.kape.ui.tv.text.SecondaryButtonText
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionManager
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun TvPerAppSettingsScreen() = Screen {
    val packageManager = LocalContext.current.packageManager
    val viewModel: SettingsViewModel = koinViewModel<SettingsViewModel>().apply {
        getInstalledApplications(packageManager)
    }
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()
    val lastExcludedApps = remember { viewModel.vpnExcludedApps.value.map { it } }

    BackHandler {
        onBackPressed(viewModel, lastExcludedApps)
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
                    content = stringResource(id = R.string.per_app_settings),
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
                    Search(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        hint = stringResource(id = R.string.search),
                    ) {
                        viewModel.filterAppsByName(it, packageManager)
                    }
                    TvLazyVerticalGrid(
                        columns = TvGridCells.Fixed(1),
                        contentPadding = PaddingValues(vertical = 16.dp),
                    ) {
                        val applicationPackages = viewModel.appList.value
                        items(applicationPackages.size) { index ->
                            val applicationPackage = applicationPackages[index]
                            val icon = applicationPackage.loadIcon(packageManager)
                            val name = applicationPackage.loadLabel(packageManager).toString()
                            val excludedFromTunnel = viewModel.vpnExcludedApps.value.contains(
                                applicationPackage.packageName,
                            )
                            PerAppSettingPackageItem(
                                icon = icon,
                                name = name,
                                excludedFromTunnel = excludedFromTunnel,
                            ) {
                                if (excludedFromTunnel) {
                                    viewModel.removeFromVpnExcludedApps(
                                        applicationPackage.packageName,
                                    )
                                } else {
                                    viewModel.addToVpnExcludedApps(
                                        applicationPackage.packageName,
                                    )
                                }
                            }
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
    }

    if (viewModel.reconnectDialogVisible.value) {
        ReconnectDialog(
            onReconnect = {
                viewModel.reconnect()
                viewModel.reconnectDialogVisible.value = false
                viewModel.navigateUp()
            },
            onLater = {
                viewModel.reconnectDialogVisible.value = false
                viewModel.navigateUp()
            },
        )
    }
}

@Composable
private fun PerAppSettingPackageItem(
    icon: Drawable,
    name: String,
    excludedFromTunnel: Boolean,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        shape = ButtonDefaults.shape(
            shape = RoundedCornerShape(12.dp),
        ),
        colors = ButtonDefaults.colors(
            containerColor = LocalColors.current.background,
            contentColor = LocalColors.current.onSurfaceVariant,
            focusedContainerColor = LocalColors.current.primary,
            focusedContentColor = LocalColors.current.onPrimary,
        ),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier.size(32.dp),
                painter = rememberDrawablePainter(drawable = icon),
                contentScale = ContentScale.Fit,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(16.dp))
            SecondaryButtonText(
                modifier = Modifier.weight(1.0f),
                content = name.uppercase(),
                textAlign = TextAlign.Start,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Image(
                modifier = Modifier.size(32.dp),
                painter = if (excludedFromTunnel) {
                    painterResource(id = com.kape.settings.R.drawable.ic_locket_open)
                } else {
                    painterResource(
                        id = com.kape.settings.R.drawable.ic_locket_closed,
                    )
                },
                contentScale = ContentScale.Fit,
                contentDescription = null,
            )
        }
    }
}

private fun onBackPressed(viewModel: SettingsViewModel, lastExcludedApps: List<String>) {
    if (viewModel.isConnected() && lastExcludedApps != viewModel.vpnExcludedApps.value) {
        viewModel.showReconnectDialogIfVpnConnected()
    } else {
        viewModel.navigateUp()
    }
}