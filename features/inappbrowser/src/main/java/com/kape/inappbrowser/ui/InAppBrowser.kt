package com.kape.inappbrowser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.kape.appbar.view.AppBar
import com.kape.appbar.view.AppBarType
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.ui.elements.Screen
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun InAppBrowser(url: String) = Screen {
    val appBarViewModel: AppBarViewModel = koinViewModel()
    val viewModel: InAppBrowserViewModel = koinViewModel()
    val state = rememberWebViewState(url = url)

    val loadingState = state.loadingState

    // workaround not to block screen drawing https://github.com/google/accompanist/issues/1442
    WebView(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        onCreated = {
            it.settings.javaScriptEnabled = true
        },
    )

    Column(modifier = Modifier.fillMaxSize()) {
        AppBar(
            viewModel = appBarViewModel,
            type = AppBarType.InAppBrowser,
            onLeftIconClick = { viewModel.navigateBack() },
        )

        if (loadingState is LoadingState.Loading) {
            LinearProgressIndicator(
                progress = loadingState.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = LocalColors.current.primary,
            )
        }
    }
}