package com.kape.customization.data

import kotlinx.serialization.Serializable

@Serializable
data class ScreenElement(
    val element: Element,
    var isVisible: Boolean = true,
    var isLocked: Boolean = false,
)