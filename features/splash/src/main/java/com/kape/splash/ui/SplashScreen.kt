package com.kape.splash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kape.splash.ui.vm.SplashViewModel
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(viewModel: SplashViewModel = koinViewModel()) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalColors.current.background),
    ) {
        Image(
            painter = painterResource(id = com.kape.ui.R.drawable.ic_pia_logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .width(200.dp)
                .height(80.dp),
        )
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.load()
    }
}