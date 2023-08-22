package com.kape.ui.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kape.ui.theme.ButtonBackground
import com.kape.ui.theme.Height
import com.kape.ui.utils.LocalColors

@Composable
fun SecondaryButton(modifier: Modifier, properties: ButtonProperties) {
    OutlinedButton(
        onClick = properties.onClick,
        border = BorderStroke(1.dp, LocalColors.current.primary),
        shape = ButtonBackground,
        modifier = modifier
            .semantics { contentDescription = properties.label }
            .testTag(properties.label)
            .height(Height.DEFAULT)
            .fillMaxWidth(),
        enabled = properties.enabled,
    ) {
        Text(text = properties.label, color = LocalColors.current.primary)
    }
}