package com.kape.uicomponents.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp)
)

val InputFieldBackground = RoundedCornerShape(Corner.InputField, Corner.InputField)
val ButtonBackground = RoundedCornerShape(Corner.Button)
val OutlineBackground = RoundedCornerShape(Corner.InputField)