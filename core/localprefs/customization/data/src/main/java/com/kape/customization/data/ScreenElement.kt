package com.kape.customization.data

import kotlinx.serialization.Serializable

@Serializable
data class ScreenElement(
    val element: Element,
    val name: String,
    var isVisible: Boolean = true,
    var isLocked: Boolean = false,
)