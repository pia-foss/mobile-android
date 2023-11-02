package com.kape.ui.elements

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.kape.router.Back
import com.kape.router.Router
import org.koin.compose.koinInject

@Composable
fun Screen(content: @Composable () -> Unit) {
    val router: Router = koinInject()
    BackHandler {
        router.handleFlow(Back)
    }
    content()
}