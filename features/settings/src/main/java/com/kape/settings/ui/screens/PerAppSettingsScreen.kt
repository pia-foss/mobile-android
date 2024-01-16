package com.kape.settings.ui.screens

import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.kape.appbar.view.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.elements.Screen
import com.kape.ui.elements.Search
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun PerAppSettingsScreen() = Screen {
    val packageManager = LocalContext.current.packageManager
    val viewModel: SettingsViewModel = koinViewModel<SettingsViewModel>().apply {
        getInstalledApplications(packageManager)
    }
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.per_app_settings))
    }

    BackHandler {
        viewModel.navigateUp()
    }

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
                onLeftIconClick = { viewModel.navigateUp() },
            )
        },
    ) {
        Column(
            Modifier
                .padding(it)
                .fillMaxHeight()
                .background(LocalColors.current.background),
        ) {
            Search(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                hint = "",
            ) {
                viewModel.filterAppsByName(it, packageManager)
            }
            LazyColumn {
                val items = viewModel.appList.value
                items(items.size) { index ->
                    val item = items[index]
                    val isExcluded =
                        viewModel.vpnExcludedApps.value.contains(item.packageName)
                    AppRow(
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
}

@Composable
private fun AppRow(
    icon: Drawable,
    name: String,
    isExcluded: Boolean,
    onClick: (name: String, isExcluded: Boolean) -> Unit,
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clickable {
                onClick(name, !isExcluded)
            },
    ) {
        val (image, text, button) = createRefs()

        Icon(
            painter = rememberDrawablePainter(drawable = icon),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .constrainAs(image) {
                    start.linkTo(parent.start, margin = 16.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .size(48.dp),
        )

        Text(
            text = name,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .constrainAs(text) {
                    start.linkTo(image.end, margin = 16.dp)
                    end.linkTo(button.start, margin = 16.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                },
        )

        LockCheckBox(
            checked = isExcluded,
            modifier = Modifier.constrainAs(button) {
                end.linkTo(parent.end, margin = 16.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
        )
    }
}

@Composable
private fun LockCheckBox(checked: Boolean, modifier: Modifier) {
    Icon(
        painter = if (checked) {
            painterResource(id = R.drawable.ic_locket_open)
        } else {
            painterResource(
                id = R.drawable.ic_locket_closed,
            )
        },
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = modifier.size(24.dp),
    )
}