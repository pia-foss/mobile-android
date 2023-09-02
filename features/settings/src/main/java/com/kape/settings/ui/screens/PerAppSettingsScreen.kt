package com.kape.settings.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.kape.appbar.view.NavigationAppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.settings.utils.PerAppSettingsUtils
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerAppSettingsScreen() {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.privacy))
    }
    val packageManager = LocalContext.current.packageManager
    Scaffold(
        topBar = {
            NavigationAppBar(
                viewModel = appBarViewModel,
                onLeftButtonClick = {
                    viewModel.navigateUp()
                },
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.padding(it),
        ) {
            val items = PerAppSettingsUtils.getInstalledApps(packageManager)
            items(items.size) { index ->
                val item = items[index]
                val isExcluded =
                    viewModel.vpnExcludedApps.value.contains(item.loadLabel(packageManager))
                AppRow(
                    icon = item.loadIcon(packageManager),
                    name = item.loadLabel(packageManager).toString(),
                    isExcluded = isExcluded,
                    onClick = { name, isChecked ->
                        if (isChecked) {
                            viewModel.addToVpnExcludedApps(name)
                        } else {
                            viewModel.removeFromVpnExcludedApps(name)

                        }
                    },
                )
                Divider(color = LocalColors.current.outline)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp)
            .clickable {
                onClick(name, !isExcluded)
            },
    ) {
        Icon(
            painter = rememberDrawablePainter(drawable = icon),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .size(36.dp)
                .align(CenterVertically),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = name,
            modifier = Modifier.align(CenterVertically),
        )
        Spacer(modifier = Modifier.weight(1f))
        LockCheckBox(checked = isExcluded, Modifier.align(CenterVertically))
    }
}

@Composable
private fun LockCheckBox(checked: Boolean, modifier: Modifier) {
    Icon(
        painter = if (checked) painterResource(id = R.drawable.ic_locket_open) else painterResource(
            id = R.drawable.ic_locket_closed,
        ),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = modifier.size(24.dp),
    )
}