package com.kape.splash.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.kape.splash.ui.vm.SplashViewModel
import com.kape.ui.elements.UiResources
import com.kape.ui.theme.Space
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(viewModel: SplashViewModel = koinViewModel()) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            viewModel.load()
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = UiResources.bigAppLogo),
            contentDescription = "logo",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(Space.HUGE)
        )
    }

    LaunchedEffect(key1 = Unit) {
        if (!viewModel.isNotificationPermissionGranted()) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.load()
        }
    }
}