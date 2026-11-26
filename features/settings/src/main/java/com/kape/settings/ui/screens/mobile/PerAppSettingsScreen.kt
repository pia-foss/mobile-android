package com.kape.settings.ui.screens.mobile

import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.ui.elements.ReconnectPromptDialog
import com.kape.settings.ui.vm.PerAppSettingsViewModel
import com.kape.ui.R
import com.kape.ui.Screen
import com.kape.ui.mobile.elements.Search
import com.kape.ui.utils.LocalColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PerAppSettingsScreen() =
    Screen {
        val packageManager = LocalContext.current.packageManager
        val viewModel: PerAppSettingsViewModel = koinViewModel()
        val appListState by viewModel.appList.collectAsStateWithLifecycle()
        val appBarViewModel: AppBarViewModel =
            koinViewModel<AppBarViewModel>().apply {
                appBarText(stringResource(id = R.string.per_app_settings))
            }
        val excluded by viewModel.vpnExcludedApps.collectAsStateWithLifecycle()
        val lastExcludedApps = remember { excluded.map { it } }

        BackHandler {
            onBackPressed(viewModel, lastExcludedApps, excluded, appBarViewModel::navigateBack)
        }

        Scaffold(
            topBar = {
                AppBar(
                    viewModel = appBarViewModel,
                    onLeftIconClick = {
                        onBackPressed(
                            viewModel,
                            lastExcludedApps,
                            excluded,
                            appBarViewModel::navigateBack,
                        )
                    },
                )
            },
        ) {
            Column(
                Modifier
                    .padding(it)
                    .fillMaxWidth()
                    .background(LocalColors.current.background),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(modifier = Modifier.widthIn(max = 520.dp)) {
                    Search(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                    ) {
                        viewModel.filterAppsByName(it)
                    }
                    if (appListState.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn {
                        val items = appListState
                        items(items.size) { index ->
                            val item = items[index]
                            val isExcluded =
                                excluded.contains(item.packageName)
                            ApplicationRow(
                                icon = item.loadIcon(packageManager),
                                name = item.loadLabel(packageManager).toString(),
                                isExcluded = isExcluded,
                                onClick = { name, isChecked ->
                                    if (isChecked) {
                                        viewModel.addToVpnExcludedApps(item.packageName)
                                    } else {
                                        viewModel.removeFromVpnExcludedApps(item.packageName)
                                    }
                                },
                            )
                        }
                    }
                }
            }
            ReconnectPromptDialog(viewModel.reconnectPrompt, onDone = appBarViewModel::navigateBack)
        }
    }

@Composable
private fun ApplicationRow(
    icon: Drawable,
    name: String,
    isExcluded: Boolean,
    onClick: (name: String, isExcluded: Boolean) -> Unit,
) {
    ConstraintLayout(
        modifier =
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .selectable(
                    selected = !isExcluded,
                    onClick = {
                        onClick(name, !isExcluded)
                    },
                ),
    ) {
        val (image, text, button) = createRefs()

        Icon(
            painter = rememberDrawablePainter(drawable = icon),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier =
                Modifier
                    .constrainAs(image) {
                        start.linkTo(parent.start, margin = 16.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }.size(48.dp),
        )

        Text(
            text = name,
            textAlign = TextAlign.Start,
            modifier =
                Modifier
                    .constrainAs(text) {
                        start.linkTo(image.end, margin = 16.dp)
                        end.linkTo(button.start, margin = 16.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    },
        )

        SelectedCheckBox(
            checked = isExcluded,
            modifier =
                Modifier.constrainAs(button) {
                    end.linkTo(parent.end, margin = 16.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
        )
    }
}

@Composable
private fun SelectedCheckBox(
    checked: Boolean,
    modifier: Modifier,
) {
    Icon(
        painter =
            if (checked) {
                painterResource(id = com.kape.settings.R.drawable.ic_locket_open)
            } else {
                painterResource(
                    id = com.kape.settings.R.drawable.ic_locket_closed,
                )
            },
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = modifier.size(24.dp),
    )
}

private fun onBackPressed(
    viewModel: PerAppSettingsViewModel,
    lastExcludedApps: List<String>,
    excluded: List<String>,
    navigateBack: () -> Unit,
) {
    if (viewModel.isConnected() && lastExcludedApps != excluded) {
        viewModel.reconnectPrompt.onSettingChanged()
    } else {
        navigateBack()
    }
}