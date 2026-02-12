package com.kape.inappbrowser.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.view.mobile.AppBarType
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppBrowser(url: String) = Screen {
    val appBarViewModel: AppBarViewModel = koinViewModel()
    val viewModel: InAppBrowserViewModel = koinViewModel()

    var loading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val webview = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            setBackgroundColor(android.graphics.Color.TRANSPARENT) // important
        }
    }

    LaunchedEffect(url) {
        webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                loading = false
            }
        }
        webview.loadUrl(url)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AppBar(
            viewModel = appBarViewModel,
            type = AppBarType.InAppBrowser,
            onLeftIconClick = { viewModel.navigateBack() },
        )

        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = LocalColors.current.primary,
            )
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { webview }
            )
        }
    }
}