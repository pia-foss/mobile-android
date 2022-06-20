package com.kape.uicomponents.components

import android.net.Uri

data class InputFieldProperties(val label: String, var error: String? = null, val maskInput: Boolean, var content: String = "")

data class ButtonProperties(val label: String, val enabled: Boolean, val onClick: () -> Unit)

data class WebViewComponentProperties(
        val url: Uri,
        val userAgentString: String = "",
        val headers: List<Header> = emptyList(),
        val onPageTitle: (String) -> Unit = {},
        val javaScriptEnabled: Boolean = false,
        val maxWebViewRestart: Int = 3
)

data class Header(val key: String, val value: String)
