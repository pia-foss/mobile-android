package com.kape.splash

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kape.splash.ui.SplashViewModel
import com.kape.ui.Screen
import com.kape.ui.utils.LocalColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(viewModel: SplashViewModel = koinViewModel()) =
    Screen {
        val activity = LocalActivity.current
        BackHandler {
            activity?.finish()
        }
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(LocalColors.current.background),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(id = com.kape.ui.R.drawable.ic_logo_large),
                contentDescription = null,
                modifier = Modifier.width(200.dp),
            )
        }

        LaunchedEffect(key1 = Unit) {
            viewModel.load(activity)
            if (activity != null && viewModel.shouldPreloadPrices()) {
                viewModel.loadPrices(activity)
            }
        }
    }