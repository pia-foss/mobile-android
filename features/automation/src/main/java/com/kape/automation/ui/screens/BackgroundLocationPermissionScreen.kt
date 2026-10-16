package com.kape.automation.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.automation.ui.viewmodel.AutomationViewModel
import com.kape.ui.mobile.elements.Screen
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun BackgroundLocationPermissionScreen() =
    Screen {
        val viewModel: AutomationViewModel = koinViewModel()
        val appBarViewModel: AppBarViewModel =
            koinViewModel<AppBarViewModel>().apply {
                appBarText(stringResource(id = com.kape.ui.R.string.trusted_network_plural))
            }
        val context = LocalContext.current
        val launcher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = {
                    if (it) {
                        viewModel.onBackgroundLocationPermissionGranted(context)
                    }
                },
            )

        Scaffold(
            topBar = {
                AppBar(viewModel = appBarViewModel)
            },
        ) {
            LocationPermissionContent(
                title = stringResource(id = com.kape.ui.R.string.background_location_permissions_title),
                message = stringResource(id = com.kape.ui.R.string.background_location_permissions_message),
                action = stringResource(id = com.kape.ui.R.string.background_location_permissions_action),
                actionTestTag = ":BackgroundLocationPermissionScreen:action",
                onActionClick = { launcher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION) },
                modifier = Modifier.padding(it),
            )
        }
    }