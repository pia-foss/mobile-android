package com.kape.uicomponents.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kape.uicomponents.theme.FontSize
import com.kape.uicomponents.theme.Grey55
import com.kape.uicomponents.theme.Space

@Composable
fun ConnectionTile(labelId: Int, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Space.NORMAL)
    ) {
        Text(
            text = stringResource(id = labelId),
            color = Grey55,
            fontSize = FontSize.Small
        )
        Spacer(modifier = Modifier.height(Space.SMALL))
        content()
    }
}
