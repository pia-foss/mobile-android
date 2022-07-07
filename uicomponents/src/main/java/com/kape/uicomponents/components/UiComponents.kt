package com.kape.uicomponents.components

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kape.uicomponents.theme.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max
import kotlin.math.min


@Composable
fun InputField(modifier: Modifier, properties: InputFieldProperties) {
    var content by remember { mutableStateOf("") }
    Column(modifier) {
        TextField(value = content,
            onValueChange = {
                content = it
                properties.content = it
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = White,
                focusedIndicatorColor = Grey92,
                unfocusedIndicatorColor = Grey92,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                cursorColor = Color.Black,
                textColor = Color.Black
            ),
            visualTransformation = if (properties.maskInput) PasswordVisualTransformation() else VisualTransformation.None,
            shape = InputFieldBackground,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = properties.label },
            label = { Text(properties.label) })
        properties.error?.let {
            Text(text = it, color = Color.Red, modifier = Modifier
                .padding(vertical = Space.SMALL_VERTICAL)
                .semantics { contentDescription = it }
                .testTag("errorMessage"))
        }
    }
}

@Composable
fun PrimaryButton(modifier: Modifier, properties: ButtonProperties) {
    Button(
        onClick = properties.onClick, colors = ButtonDefaults.buttonColors(
            backgroundColor = DarkGreen20,
            contentColor = White,
        ), shape = ButtonBackground,
        modifier = modifier
            .semantics { contentDescription = properties.label }
            .testTag(properties.label)
            .height(Height.DEFAULT)
            .fillMaxWidth(), enabled = properties.enabled
    ) {
        Text(text = properties.label)
    }
}

@Composable
fun NoNetworkBanner(noNetworkMessage: String) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .background(ErrorRed)) {
        Text(text = noNetworkMessage,
            color = White,
            textAlign = TextAlign.Center, modifier = Modifier
                .fillMaxWidth()
                .padding(Space.NORMAL)
        )
    }
}

private enum class WebViewLoadingState {
    UNINITIALIZED,
    LOADING,
    IDLE
}

@Composable
fun WebViewComponent(properties: WebViewComponentProperties) {
    var restartWebView: Int by remember { mutableStateOf(properties.maxWebViewRestart) }
    var loadingState: WebViewLoadingState by remember { mutableStateOf(WebViewLoadingState.UNINITIALIZED) }
    var backEnabled: Boolean by remember { mutableStateOf(false) }

    fun updateRefresh(refreshLayout: SwipeRefreshLayout) {
        when (loadingState) {
            WebViewLoadingState.UNINITIALIZED, WebViewLoadingState.LOADING -> {
                refreshLayout.isRefreshing = true
            }
            WebViewLoadingState.IDLE -> {
                refreshLayout.isRefreshing = false
            }
        }
    }

    val webClient: MyWebViewClient = remember {
        MyWebViewClient(
                properties = properties,
                backEnabled = { enabled ->
                    backEnabled = enabled
                },
                onPageTitle = properties.onPageTitle,
                onRenderProcessGone = {
                    if (restartWebView >= 0) {
                        --restartWebView
                    }
                }
        )
    }

    if (restartWebView > 0) {
        AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context: Context ->
                    val webView = WebView(context)
                    webView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

                    val refreshLayout = SwipeRefreshLayout(context)
                    refreshLayout.setColorSchemeColors(DarkGreen20.toArgb())

                    refreshLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    refreshLayout.addView(webView)
                    refreshLayout.setOnRefreshListener {
                        webView.reload()
                    }

                    webView.settings.javaScriptEnabled = properties.javaScriptEnabled

                    val userAgentString: String = properties.userAgentString
                    if (userAgentString.isNotBlank()) {
                        webView.settings.userAgentString = userAgentString.trim()
                    }

                    webView.webViewClient = webClient
                    webClient.goBack = {
                        webView.goBack()
                    }
                    webClient.onPageStarted = {
                        loadingState = WebViewLoadingState.LOADING
                        updateRefresh(refreshLayout = refreshLayout)
                    }
                    webClient.onPageFinished = {
                        loadingState = WebViewLoadingState.IDLE
                        updateRefresh(refreshLayout = refreshLayout)
                    }

                    return@AndroidView refreshLayout
                },
                update = { update: SwipeRefreshLayout ->
                    val refreshLayout: SwipeRefreshLayout = update
                    val webView: WebView? = update.getChildAt(1) as? WebView

                    if (loadingState == WebViewLoadingState.UNINITIALIZED) {
                        webView?.loadUrl(properties.url.toString())
                    }

                    updateRefresh(refreshLayout = refreshLayout)
                }
        )
    } else {
        Box(
                modifier = Modifier
                        .fillMaxSize(),
                contentAlignment = Alignment.Center
        ) {
            Text(
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp),
                    text = "WebView renderer\ncrashed ${max(a = min(a = properties.maxWebViewRestart - restartWebView, b = properties.maxWebViewRestart), b = 0)} times.",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    color = Color.Black,
            )
        }
    }

    BackHandler(backEnabled) {
        webClient.goBack()
    }
}

@Composable
fun WebViewScreen(
        initialUrl: Uri
) {
    var screenTitle: String by remember { mutableStateOf("") }

    Column {
        TopAppBar(
                modifier = Modifier.wrapContentHeight(),
                title = {
                    Text(
                            text = screenTitle,
                            maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Menu, contentDescription = "")
                    }
                },
                backgroundColor = MaterialTheme.colors.primaryVariant
        )

        WebViewComponent(
                properties = WebViewComponentProperties(
                        url = initialUrl,
                        userAgentString = userAgentString(versionName = "3.16.0", versionCode = "576"),
                        onPageTitle = { title ->
                            if (screenTitle != title) {
                                screenTitle = title
                                Log.i("WebViewScreenPrivacyPolicy", "screen title: '$title'")
                            }
                        },
                        javaScriptEnabled = true
                )
        )
    }
}

@Preview
@Composable
fun WebViewScreenPrivacyPolicy() {
    WebViewScreen(
            initialUrl = Uri.Builder().apply {
                scheme("https")
                authority("www.privateinternetaccess.com")
                path("privacy-policy")
            }.build()
    )
}

@Preview
@Composable
fun WebViewScreenHomePage() {
    WebViewScreen(
            initialUrl = Uri.Builder().apply {
                scheme("https")
                authority("www.privateinternetaccess.com")
            }.build()
    )
}

@Preview
@Composable
fun WebViewScreenSupport() {
    WebViewScreen(
            initialUrl = Uri.Builder().apply {
                scheme("https")
                authority("helpdesk.privateinternetaccess.com")
            }.build()
    )
}

fun userAgentString(
        versionName: String,
        versionCode: String
): String = "privateinternetaccess.com Android Client/$versionName($versionCode)"

private class MyWebViewClient(
        private val properties: WebViewComponentProperties,
        private val backEnabled: (Boolean) -> Unit,
        private val onPageTitle: (String) -> Unit,
        private val onRenderProcessGone: (Boolean) -> Unit
) : WebViewClient() {
    var goBack: () -> Unit = {}
    var onPageStarted: () -> Unit = {}
    var onPageFinished: () -> Unit = {}

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        backEnabled(view.canGoBack())
        updatePageTitle(view = view)
        if (url.isNullOrBlank().not()) {
            onPageStarted()
        }
    }

    override fun onPageFinished(view: WebView, url: String?) {
        backEnabled(view.canGoBack())
        updatePageTitle(view = view)
        if (url.isNullOrBlank().not()) {
            onPageFinished()
        }
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? = when {
        properties.headers.isNotEmpty() && request.url == properties.url -> doRequestWithHeaders(request.url)
        else -> doRequestNormally()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return true
        }

        // clean up web view
        view.destroy()

        onRenderProcessGone(detail.didCrash())

        // never crash application
        return true
    }


    private fun updatePageTitle(view: WebView) {
        val pageTitle: String = view.title ?: ""
        onPageTitle(when {
            pageTitle.isNotBlank() && "(http(s)?|file)://.*".toRegex(RegexOption.IGNORE_CASE).matches(pageTitle).not() -> pageTitle.trim()
            else -> ""
        })
    }

    private fun doRequestNormally(): WebResourceResponse? = null

    private fun doRequestWithHeaders(url: Uri): WebResourceResponse {
        try {
            val connection: HttpURLConnection = URL(url.toString()).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val userAgentString: String = properties.userAgentString
            if (userAgentString.isNotBlank()) {
                connection.setRequestProperty("user-agent", userAgentString.trim())
            }

            properties.headers.forEach { header: Header ->
                if ("user-agent".equals(other = header.key.trim(), ignoreCase = true)) {
                    return@forEach
                }
                connection.setRequestProperty(header.key.trim(), header.value.trim())
            }

            val responseHeaders: Map<String, String> = connection.headerFields.mapValuesTo(mutableMapOf<String, String>()) { values -> values.value.firstOrNull() ?: "" }.filterValues { v -> v.isNotBlank() }
            return WebResourceResponse(connection.contentType, connection.contentEncoding, connection.responseCode, connection.responseMessage, responseHeaders, connection.inputStream)
        } catch (t: Throwable) {
            return WebResourceResponse(null, null, 503, "unavailable", null, null)
        }
    }
}
