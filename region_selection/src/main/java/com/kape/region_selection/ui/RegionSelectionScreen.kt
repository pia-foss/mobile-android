package com.kape.region_selection.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import com.kape.region_selection.ui.vm.RegionSelectionViewModel
import com.kape.region_selection.utils.IDLE
import com.kape.region_selection.utils.LOADING
import org.koin.androidx.compose.viewModel

@Composable
fun RegionSelectionScreen() {
    val viewModel: RegionSelectionViewModel by viewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0].language

    LaunchedEffect(Unit) {
        viewModel.loadRegions(locale)
    }

    when (state) {
        IDLE -> {

        }
        LOADING -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        else -> {
            // state loaded
        }
    }
}