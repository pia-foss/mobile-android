package com.kape.settings.utils

data class ButtonProperty(val type: ButtonType, val label: String? = null)

sealed class ButtonType {
    data object Positive : ButtonType()
    data object Negative : ButtonType()
    data object Neutral : ButtonType()
}

fun getDefaultButtons(): Map<ButtonType, ButtonProperty> = mapOf(
    ButtonType.Positive to
        ButtonProperty(type = ButtonType.Positive, null),
    ButtonType.Negative to
        ButtonProperty(type = ButtonType.Negative, null),
)