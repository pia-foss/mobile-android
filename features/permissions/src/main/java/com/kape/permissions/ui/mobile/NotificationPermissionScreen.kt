package com.kape.permissions.ui.mobile

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.kape.permissions.ui.vm.PermissionsViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.InsetsColumn
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.text.OnboardingDescriptionText
import com.kape.ui.mobile.text.OnboardingTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NotificationPermissionScreen() = Screen {
    val viewModel: PermissionsViewModel = koinViewModel()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            viewModel.exitOnboarding()
        },
    )

    LaunchedEffect(key1 = Unit) {
        if (viewModel.isNotificationPermissionGranted()) {
            viewModel.exitOnboarding()
        }
    }
    InsetsColumn {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(LocalColors.current.background)
                .semantics {
                    testTagsAsResourceId = true
                },
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.pia_medium),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(16.dp)
                        .height(40.dp)
                        .fillMaxWidth(),
                )
                Image(
                    painter = painterResource(id = com.kape.permissions.R.drawable.image_bell),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(40.dp)
                        .height(140.dp)
                        .fillMaxWidth(),
                )
                Column(modifier = Modifier.semantics(mergeDescendants = true) { }) {
                    OnboardingTitleText(
                        content = stringResource(id = R.string.notifications_title),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    )

                    OnboardingDescriptionText(
                        content = stringResource(id = R.string.notifications_description),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                PrimaryButton(
                    text = stringResource(id = R.string.notifications_action),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp, bottom = 36.dp, end = 16.dp)
                        .align(Alignment.CenterHorizontally)
                        .testTag(":NotificationPermissionScreen:notifications_action"),
                ) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}