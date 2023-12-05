package com.kape.settings.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.R
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.elements.Screen
import com.kape.ui.elements.Separator
import com.kape.ui.theme.FontSize
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.toColor
import com.kape.ui.utils.toColorString
import com.raedapps.alwan.rememberAlwanState
import com.raedapps.alwan.ui.AlwanDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun WidgetSettingsScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.settings_widget_configuration))
    }
    val showDialog = remember { mutableStateOf(false) }
    val selectedColorType = remember { mutableStateOf<ColorSection>(ColorSection.None) }

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
            ) {
                viewModel.exitWidgetSettings()
            }
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it),
        ) {
            ColorSettingItem(
                titleId = R.string.widget_background,
                color = viewModel.getWidgetSettings().background.toColor(),
            ) {
                selectedColorType.value = ColorSection.Background
                showDialog.value = true
            }
            ColorSettingItem(
                titleId = R.string.widget_text,
                color = viewModel.getWidgetSettings().text.toColor(),
            ) {
                selectedColorType.value = ColorSection.Text
                showDialog.value = true
            }
            ColorSettingItem(
                titleId = R.string.widget_icon_upload,
                color = viewModel.getWidgetSettings().uploadIcon.toColor(),
            ) {
                selectedColorType.value = ColorSection.Upload
                showDialog.value = true
            }
            ColorSettingItem(
                titleId = R.string.widget_icon_download,
                color = viewModel.getWidgetSettings().downloadIcon.toColor(),
            ) {
                selectedColorType.value = ColorSection.Download
                showDialog.value = true
            }
            Separator()
            Column(
                modifier = Modifier
                    .defaultMinSize(56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable {
                        // TODO: show options dialog (if still needed)
                    },
            ) {
                Text(
                    text = stringResource(id = R.string.widget_radius),
                    fontSize = FontSize.Normal,
                    color = LocalColors.current.onSurface,
                )
                Text(
                    text = viewModel.getWidgetSettings().radius.toString(),
                    fontSize = FontSize.Normal,
                    color = LocalColors.current.onSurfaceVariant,
                )
            }
            if (showDialog.value) {
                val settings = viewModel.getWidgetSettings()
                val currentSelection = selectedColorType.value
                ColorPickerDialog(
                    showDialog = showDialog,
                    which = currentSelection,
                    current = when (currentSelection) {
                        ColorSection.Background -> settings.background.toColor()
                        ColorSection.Download -> settings.downloadIcon.toColor()
                        ColorSection.Text -> settings.text.toColor()
                        ColorSection.Upload -> settings.uploadIcon.toColor()
                        ColorSection.None -> Color.Unspecified
                    },
                ) { color, which ->
                    when (which) {
                        ColorSection.Background -> {
                            settings.background = color.toColorString()
                        }

                        ColorSection.Download -> {
                            settings.downloadIcon = color.toColorString()
                        }

                        ColorSection.Text -> {
                            settings.text = color.toColorString()
                        }

                        ColorSection.Upload -> {
                            settings.uploadIcon = color.toColorString()
                        }

                        ColorSection.None -> {
                            // default state
                        }
                    }
                    viewModel.updateWidgetSettings(settings)
                }
            }
        }
    }
}

@Composable
fun ColorSettingItem(@StringRes titleId: Int, color: Color, onClick: () -> Unit) {
    Row {
        Text(text = stringResource(id = titleId), modifier = Modifier.padding(16.dp))
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .clickable {
                    onClick()
                },
        ) {}
    }
}

@Composable
fun ColorPickerDialog(
    showDialog: MutableState<Boolean>,
    which: ColorSection,
    current: Color,
    onUpdate: (color: Color, which: ColorSection) -> Unit,
) {
    val selectionColor = remember { mutableStateOf(current) }
    AlwanDialog(
        onDismissRequest = { showDialog.value = false },
        onColorChanged = {
            selectionColor.value = it
        },
        positiveButtonText = stringResource(id = R.string.ok),
        onPositiveButtonClick = {
            showDialog.value = false
            onUpdate(selectionColor.value, which)
        },
        negativeButtonText = stringResource(id = R.string.cancel),
        onNegativeButtonClick = {
            showDialog.value = false
        },
        state = rememberAlwanState(initialColor = current),
    )
}

sealed class ColorSection {
    data object Background : ColorSection()
    data object Text : ColorSection()
    data object Upload : ColorSection()
    data object Download : ColorSection()
    data object None : ColorSection()
}