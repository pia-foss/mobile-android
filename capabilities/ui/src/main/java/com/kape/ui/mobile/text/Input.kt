package com.kape.ui.mobile.text

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.kape.ui.R
import com.kape.ui.utils.LocalColors

@Composable
fun Input(
    modifier: Modifier,
    label: String? = null,
    maskInput: Boolean,
    singleLine: Boolean = false,
    keyboard: KeyboardType,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Default,
    platformImeOptions: PlatformImeOptions? = null,
    content: MutableState<String>,
    errorMessage: String? = null,
) {
    val trailingIcon: @Composable (() -> Unit)? = errorMessage?.let {
        @Composable {
            Icon(
                painter = painterResource(id = R.drawable.ic_error),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        }
    }
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
            singleLine = singleLine,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = trailingIcon,
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
                imeAction = imeAction,
                platformImeOptions = platformImeOptions,
            ),
            keyboardActions = keyboardActions,
            isError = errorMessage != null,
        )
        errorMessage?.let {
            InputErrorText(it, Modifier.padding(top = 4.dp))
        }
    }
}