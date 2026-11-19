package com.kape.settings.ui.elements

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.settings.ui.vm.MockLocationViewModel

class MockLocationToggleState(
    val enabled: Boolean,
    val toggle: (Boolean) -> Unit,
)

@Composable
fun rememberMockLocationToggleState(viewModel: MockLocationViewModel): MockLocationToggleState {
    val settingEnabled by viewModel.mockLocationEnabled.collectAsStateWithLifecycle()
    val appSelected by viewModel.mockLocationAppSelected.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refreshMockLocationAppSelected()
        onPauseOrDispose { }
    }

    val enableOrStartSetup = {
        if (viewModel.refreshMockLocationAppSelected()) {
            viewModel.toggleMockLocation(true)
        } else {
            viewModel.navigateToMockLocationSettings()
        }
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) enableOrStartSetup()
        }
    return MockLocationToggleState(settingEnabled && appSelected) { enabled ->
        when {
            !enabled -> viewModel.toggleMockLocation(false)
            !viewModel.isLocationPermissionGranted() -> permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            else -> enableOrStartSetup()
        }
    }
}

fun openDeveloperSettings(context: Context) {
    val developerOptionsEnabled =
        Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
    val action =
        if (developerOptionsEnabled) {
            Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
        } else {
            Settings.ACTION_DEVICE_INFO_SETTINGS
        }
    try {
        context.startActivity(Intent(action))
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Settings.ACTION_SETTINGS))
    }
}

@Composable
fun MockLocationSetupEffect(viewModel: MockLocationViewModel) {
    LifecycleResumeEffect(Unit) {
        if (viewModel.refreshMockLocationAppSelected()) {
            viewModel.onMockLocationAppSelected()
        }
        onPauseOrDispose { }
    }
}