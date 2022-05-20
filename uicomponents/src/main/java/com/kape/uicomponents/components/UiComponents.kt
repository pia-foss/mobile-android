package com.kape.uicomponents.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kape.uicomponents.theme.*


@Composable
fun InputField(modifier: Modifier, properties: InputFieldProperties) {
    var content by remember { mutableStateOf("") }
    Column(modifier) {
        TextField(value = content,
            onValueChange = {
                content = it
                properties.content = it
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = White,
                focusedIndicatorColor = Grey92,
                unfocusedIndicatorColor = Grey92,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                cursorColor = Color.Black,
                textColor = Color.Black
            ),
            visualTransformation = if (properties.maskInput) PasswordVisualTransformation() else VisualTransformation.None,
            shape = InputFieldBackground,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = properties.label },
            label = { Text(properties.label) })
        properties.error?.let {
            Text(text = it, color = Color.Red, modifier = Modifier
                .padding(vertical = Space.SMALL_VERTICAL)
                .semantics { contentDescription = it }
                .testTag("errorMessage"))
        }
    }
}

@Composable
fun PrimaryButton(modifier: Modifier, properties: ButtonProperties) {
    Button(
        onClick = properties.onClick, colors = ButtonDefaults.buttonColors(
            backgroundColor = DarkGreen20,
            contentColor = White,
        ), shape = ButtonBackground,
        modifier = modifier
            .semantics { contentDescription = properties.label }
            .height(Height.DEFAULT)
            .fillMaxWidth(), enabled = properties.enabled
    ) {
        Text(text = properties.label)
    }
}

@Composable
fun NoNetworkBanner(noNetworkMessage: String) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .background(ErrorRed)) {
        Text(text = noNetworkMessage,
            color = White,
            textAlign = TextAlign.Center, modifier = Modifier
                .fillMaxWidth()
                .padding(Space.NORMAL)
        )
    }
}
