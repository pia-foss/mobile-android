package com.kape.ui.tv.elements

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1.0f),
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
        RoundIconButton(
            modifier = Modifier.padding(start = 16.dp),
            painterId = R.drawable.ic_close,
            onClick = {
                query.value = ""
                onTextChanged("")
            },
        )
    }
}