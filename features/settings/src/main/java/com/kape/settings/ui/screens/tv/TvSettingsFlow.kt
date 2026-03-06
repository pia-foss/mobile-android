package com.kape.settings.ui.screens.tv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.router.LocalNavigator
import com.kape.settings.ui.vm.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvSettingsFlow() {
    val viewModel: SettingsViewModel = koinViewModel()
    
}