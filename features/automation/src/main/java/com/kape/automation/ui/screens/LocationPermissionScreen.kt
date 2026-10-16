package com.kape.automation.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.automation.ui.viewmodel.AutomationViewModel
import com.kape.ui.mobile.elements.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun LocationPermissionScreen() =
    Screen {
        val viewModel: AutomationViewModel = koinViewModel()
        val appBarViewModel: AppBarViewModel =
            koinViewModel<AppBarViewModel>().apply {
                appBarText(stringResource(id = com.kape.ui.R.string.trusted_network_plural))
            }
        val launcher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = {
                    if (it) {
                        viewModel.onLocationPermissionGranted()
                    }
                },
            )

        Scaffold(
            topBar = {
                AppBar(viewModel = appBarViewModel)
            },
        ) {
            LocationPermissionContent(
                title = stringResource(id = com.kape.ui.R.string.location_permission_title),
                message = stringResource(id = com.kape.ui.R.string.location_permission_message),
                action = stringResource(id = com.kape.ui.R.string.location_permission_action),
                actionTestTag = ":LocationPermissionScreen:action",
                onActionClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                modifier = Modifier.padding(it),
            )
        }
    }