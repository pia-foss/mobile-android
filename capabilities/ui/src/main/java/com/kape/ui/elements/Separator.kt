package com.kape.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kape.ui.theme.Height
import com.kape.ui.utils.LocalColors

@Composable
fun Separator() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(Height.SEPARATOR)
        .background(LocalColors.current.outline))
}