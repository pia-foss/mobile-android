package com.kape.ui.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.kape.ui.theme.InputFieldBackground
import com.kape.ui.theme.Space
import com.kape.ui.utils.LocalColors

@Deprecated("to be replaced by Input")
@Composable
fun InputField(modifier: Modifier, properties: InputFieldProperties) {
    var content by remember { properties.content }
    Column(modifier) {
        TextField(
            value = content,
            onValueChange = {
                content = it
                properties.content.value = it
            },
            visualTransformation = if (properties.maskInput) PasswordVisualTransformation() else VisualTransformation.None,
            shape = InputFieldBackground,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = properties.label },
            label = { Text(properties.label) },
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                keyboardType = properties.keyboardType,
            ),
        )
        properties.error?.let {
            Text(
                text = it,
                color = LocalColors.current.error,
                modifier = Modifier
                    .padding(vertical = Space.SMALL_VERTICAL)
                    .semantics { contentDescription = it }
                    .testTag("errorMessage"),
            )
        }
    }
}