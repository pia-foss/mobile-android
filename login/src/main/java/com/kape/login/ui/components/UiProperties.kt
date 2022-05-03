package com.kape.login.ui.components

data class InputFieldProperties(val label: String, var error: String? = null, val maskInput: Boolean, var content: String = "")

data class ButtonProperties(val label: String, val enabled: Boolean, val onClick: () -> Unit)