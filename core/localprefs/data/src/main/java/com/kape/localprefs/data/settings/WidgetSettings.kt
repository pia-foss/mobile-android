package com.kape.settings.data

import kotlinx.serialization.Serializable

@Serializable
data class WidgetSettings(
    var background: String,
    var text: String,
    var uploadIcon: String,
    var downloadIcon: String,
    var radius: Int,
)