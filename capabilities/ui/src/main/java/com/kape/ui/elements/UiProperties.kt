package com.kape.ui.elements

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.KeyboardType

@Deprecated("to be removed as part of UI Overhaul. To be replaced with something more suitable.")

data class InputFieldProperties(
    val label: String,
    var error: String? = null,
    val maskInput: Boolean,
    val keyboardType: KeyboardType = KeyboardType.Text,
    var content: MutableState<String> = mutableStateOf("")
)

data class ButtonProperties(val label: String, val enabled: Boolean = true, val onClick: () -> Unit)

data class WebViewComponentProperties(
    val url: Uri,
    val userAgentString: String = "",
    val headers: List<Header> = emptyList(),
    val onPageTitle: (String) -> Unit = {},
    val javaScriptEnabled: Boolean = false,
    val maxWebViewRestart: Int = 3,
)

data class Header(val key: String, val value: String)