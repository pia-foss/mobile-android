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
import com.kape.settings.ui.vm.SettingsViewModel

class MockLocationToggleState(
    val enabled: Boolean,
    val toggle: (Boolean) -> Unit,
)

/**
 * Mock location only takes effect when the setting is on and PIA is the selected mock location
 * app, so the toggle shows both. Enabling it needs the fine location permission (for the location
 * foreground service); if PIA isn't selected yet, the user is taken through the setup screen.
 */
@Composable
fun rememberMockLocationToggleState(viewModel: SettingsViewModel): MockLocationToggleState {
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

/**
 * Opens Developer options, or the About screen when Developer options aren't turned on yet
 * (the user enables them there by tapping Build number).
 */
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

/**
 * Re-checks the mock location app whenever the setup screen resumes, i.e. when the user comes back
 * from Developer options, and finishes the setup once PIA is selected.
 */
@Composable
fun MockLocationSetupEffect(viewModel: SettingsViewModel) {
    LifecycleResumeEffect(Unit) {
        if (viewModel.refreshMockLocationAppSelected()) {
            viewModel.onMockLocationAppSelected()
        }
        onPauseOrDispose { }
    }
}