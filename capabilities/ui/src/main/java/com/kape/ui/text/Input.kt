package com.kape.ui.text

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.kape.ui.R
import com.kape.ui.utils.LocalColors

@Composable
fun Input(
    modifier: Modifier,
    label: String? = null,
    maskInput: Boolean,
    keyboard: KeyboardType,
    content: MutableState<String>,
    errorMessage: String? = null,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = content.value,
            onValueChange = {
                content.value = it
            },
            label = {
                label?.let {
                    InputLabelText(content = label)
                }
            },
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                errorMessage?.let {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_error),
                        contentDescription = null,
                        tint = Color.Unspecified,
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LocalColors.current.onPrimary,
                unfocusedContainerColor = LocalColors.current.onPrimary,
                disabledContainerColor = LocalColors.current.onPrimary,
                focusedBorderColor = LocalColors.current.outlineVariant,
                errorBorderColor = LocalColors.current.error,
            ),
            visualTransformation = if (maskInput) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                keyboardType = keyboard,
            ),
            isError = errorMessage != null,
        )
        errorMessage?.let {
            InputErrorText(it, Modifier.padding(top = 4.dp))
        }
    }
}