package com.kape.ui.mobile.elements

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.ui.R
import com.kape.ui.mobile.text.SearchInputLabelText
import com.kape.ui.utils.LocalColors

@Composable
fun Search(modifier: Modifier, onTextChanged: (text: String) -> Unit) {
    val query = remember { mutableStateOf("") }

    OutlinedTextField(
        modifier = modifier,
        value = query.value,
        onValueChange = {
            query.value = it
            onTextChanged(it)
        },
        placeholder = {
            SearchInputLabelText(content = stringResource(id = R.string.search))
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
            if (query.value.isNotEmpty()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.clickable {
                        query.value = ""
                        onTextChanged("")
                    },
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LocalColors.current.surfaceVariant,
            unfocusedContainerColor = LocalColors.current.surfaceVariant,
            disabledContainerColor = LocalColors.current.surfaceVariant,
            focusedBorderColor = LocalColors.current.onSurfaceVariant,
            errorBorderColor = LocalColors.current.error,
            cursorColor = LocalColors.current.onSurface,
        ),
        singleLine = true,
    )
}