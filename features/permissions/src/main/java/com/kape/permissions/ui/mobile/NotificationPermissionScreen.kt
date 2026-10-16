package com.kape.permissions.ui.mobile

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kape.permissions.ui.vm.PermissionsViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotificationPermissionScreen() =
    Screen {
        val viewModel: PermissionsViewModel = koinViewModel()
        val launcher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = {
                    viewModel.navigateToConnection()
                },
            )

        PermissionContent(
            illustration = com.kape.permissions.R.drawable.image_bell,
            title = stringResource(id = R.string.notifications_title),
            description = stringResource(id = R.string.notifications_description),
            action = stringResource(id = R.string.notifications_action),
            actionTestTag = ":NotificationPermissionScreen:notifications_action",
            onActionClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
        )
    }