package com.kape.permissions.ui.tv

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kape.permissions.ui.vm.PermissionsViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.tv.elements.PrimaryButton
import com.kape.ui.tv.text.OnboardingDescriptionText
import com.kape.ui.tv.text.OnboardingTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvNotificationPermissionScreen() = Screen {
    val viewModel: PermissionsViewModel = koinViewModel()
    val initialFocusRequester = FocusRequester()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            viewModel.exitOnboarding()
        },
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalColors.current.background),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(64.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .semantics(mergeDescendants = true) { },
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_large),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp),
                )
                Spacer(modifier = Modifier.height(32.dp))
                OnboardingTitleText(
                    content = stringResource(id = R.string.notifications_title),
                    modifier = Modifier
                        .fillMaxWidth(),
                )
                OnboardingDescriptionText(
                    content = stringResource(id = R.string.notifications_description),
                    modifier = Modifier
                        .fillMaxWidth(),
                )
            }
            PrimaryButton(
                text = stringResource(id = R.string.accept),
                modifier = Modifier
                    .focusRequester(initialFocusRequester)
                    .padding(16.dp),
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            PrimaryButton(
                text = stringResource(id = R.string.logjn_continue),
                modifier = Modifier
                    .padding(horizontal = 16.dp),
            ) {
                viewModel.exitOnboarding()
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = com.kape.permissions.R.drawable.image_bell),
                contentScale = ContentScale.Fit,
                contentDescription = null,
                modifier = Modifier
                    .size(256.dp),
            )
        }
    }

    LaunchedEffect(key1 = Unit) {
        initialFocusRequester.requestFocus()
        if (viewModel.isNotificationPermissionGranted()) {
            viewModel.exitOnboarding()
        }
    }
}