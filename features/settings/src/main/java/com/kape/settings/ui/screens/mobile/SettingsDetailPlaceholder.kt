package com.kape.settings.ui.screens.mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.mobile.AppBarBackground
import com.kape.ui.R
import com.kape.ui.mobile.text.SettingsL2TextDescription
import org.koin.androidx.compose.koinViewModel

/**
 * Fills the detail pane beside the Settings list until an item is chosen.
 * Continues the app bar band so the top edge reads as one bar across both panes.
 */
@Composable
fun SettingsDetailPlaceholder() {
    Column(modifier = Modifier.fillMaxSize()) {
        AppBarBackground(viewModel = koinViewModel())
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            SettingsL2TextDescription(content = stringResource(id = R.string.settings_detail_placeholder))
        }
    }
}