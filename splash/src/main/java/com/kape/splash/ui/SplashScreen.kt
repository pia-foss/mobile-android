package com.kape.splash.ui

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
import com.kape.uicomponents.components.UiResources
import com.kape.uicomponents.theme.Space
import org.koin.androidx.compose.inject

@Composable
fun SplashScreen() {

    val viewModel: SplashViewModel by inject()

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
        viewModel.load()
    }
}