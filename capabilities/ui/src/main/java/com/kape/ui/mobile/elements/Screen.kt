package com.kape.ui.mobile.elements

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.kape.router.LocalNavigator

@Composable
fun Screen(content: @Composable () -> Unit) {
    val navigator = LocalNavigator.current

    BackHandler {
        navigator.navigateBack()
    }
    content()
}