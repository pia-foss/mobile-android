package com.kape.login.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.kape.login.ui.theme.ButtonBackground
import com.kape.login.ui.theme.DarkGreen20
import com.kape.login.ui.theme.Grey92
import com.kape.login.ui.theme.InputFieldBackground


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
                backgroundColor = Color.White,
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
                .padding(vertical = 2.dp)
                .semantics { contentDescription = it })
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
            .height(48.dp)
            .fillMaxWidth(), enabled = properties.enabled
    ) {
        Text(text = properties.label)
    }
}
