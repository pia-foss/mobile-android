package com.kape.uicomponents.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kape.uicomponents.theme.Grey85
import com.kape.uicomponents.theme.Height

@Composable
fun Separator() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(Height.SEPARATOR)
        .background(Grey85))
}