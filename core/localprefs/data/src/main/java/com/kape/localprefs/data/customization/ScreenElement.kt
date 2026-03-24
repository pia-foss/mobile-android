package com.kape.localprefs.data.customization

import com.kape.customization.data.Element
import kotlinx.serialization.Serializable

@Serializable
data class ScreenElement(
    val element: Element,
    val name: String,
    var isVisible: Boolean = true,
    var isLocked: Boolean = false,
)