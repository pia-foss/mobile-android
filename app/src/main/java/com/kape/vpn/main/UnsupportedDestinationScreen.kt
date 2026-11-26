package com.kape.vpn.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.kape.contracts.Router
import com.kape.ui.utils.LocalColors
import com.kape.vpn.BuildConfig
import org.koin.compose.koinInject

private const val TAG = "AppNavigation"

@Composable
fun UnsupportedDestinationScreen(destination: NavKey) {
    val router: Router = koinInject()
    LaunchedEffect(destination) {
        Log.e(TAG, "No screen for ${destination::class.simpleName} on this device type")
        if (!BuildConfig.DEBUG) router.navigateBack()
    }
    if (!BuildConfig.DEBUG) return
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(LocalColors.current.background)
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No screen for ${destination::class.simpleName} on this device type",
            color = LocalColors.current.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}