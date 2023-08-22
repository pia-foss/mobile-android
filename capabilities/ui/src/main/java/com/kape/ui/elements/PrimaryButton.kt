package com.kape.ui.elements

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.kape.ui.theme.ButtonBackground
import com.kape.ui.theme.Height

@Composable
fun PrimaryButton(modifier: Modifier, properties: ButtonProperties) {
    Button(
        onClick = properties.onClick,
        shape = ButtonBackground,
        modifier = modifier
            .semantics { contentDescription = properties.label }
            .testTag(properties.label)
            .height(Height.DEFAULT)
            .fillMaxWidth(),
        enabled = properties.enabled,
    ) {
        Text(text = properties.label)
    }
}