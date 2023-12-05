package com.kape.ui.elements

import android.net.Uri

data class WebViewComponentProperties(
    val url: Uri,
    val userAgentString: String = "",
    val headers: List<Header> = emptyList(),
    val onPageTitle: (String) -> Unit = {},
    val javaScriptEnabled: Boolean = false,
    val maxWebViewRestart: Int = 3,
)

data class Header(val key: String, val value: String)