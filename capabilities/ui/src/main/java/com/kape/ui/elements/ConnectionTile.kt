package com.kape.ui.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Space
import com.kape.ui.utils.LocalColors

@Composable
fun ConnectionTile(labelId: Int, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Space.NORMAL)
    ) {
        Text(
            text = stringResource(id = labelId),
            color = LocalColors.current.outlineVariant,
            fontSize = FontSize.Small
        )
        Spacer(modifier = Modifier.height(Space.SMALL))
        content()
    }
}