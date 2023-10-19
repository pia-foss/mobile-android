package com.kape.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kape.ui.utils.LocalColors

// TODO: Rename to Separator once the deprecated file is removed. 

@Composable
fun ItemSeparator() {
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                LocalColors.current.outline,
            ),
    )
}