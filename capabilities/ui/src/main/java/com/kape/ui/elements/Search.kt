package com.kape.ui.elements

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kape.ui.R
import com.kape.ui.text.InputLabelText
import com.kape.ui.utils.LocalColors

@Composable
fun Search(modifier: Modifier, hint: String, onTextChanged: (text: String) -> Unit) {
    val query = remember { mutableStateOf("") }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth(),
        value = query.value,
        onValueChange = {
            query.value = it
        },
        placeholder = {
            InputLabelText(content = hint)
        },
        shape = RoundedCornerShape(12.dp),
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        },
        trailingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LocalColors.current.onPrimary,
            unfocusedContainerColor = LocalColors.current.onPrimary,
            disabledContainerColor = LocalColors.current.onPrimary,
            focusedBorderColor = LocalColors.current.outlineVariant,
            errorBorderColor = LocalColors.current.error,
            cursorColor = LocalColors.current.onSurface,
        ),
    )
}