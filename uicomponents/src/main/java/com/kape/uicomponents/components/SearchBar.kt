package com.kape.uicomponents.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.kape.uicomponents.R
import com.kape.uicomponents.theme.Square
import com.kape.uicomponents.theme.Space

@Composable
fun SearchBar(searchTextState: MutableState<TextFieldValue>) {
    TextField(
        value = searchTextState.value,
        onValueChange = { value ->
            searchTextState.value = value
        },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = stringResource(id = R.string.search),
                modifier = Modifier
                    .padding(Space.NORMAL)
                    .size(Square.ICON)
            )
        },
        trailingIcon = {
            if (searchTextState.value != TextFieldValue("")) {
                IconButton(
                    onClick = {
                        searchTextState.value =
                            TextFieldValue("") // Remove text from TextField when you press the 'X' icon
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = stringResource(id = R.string.close),
                        modifier = Modifier
                            .padding(Space.NORMAL)
                            .size(Square.ICON)
                    )
                }
            }
        },
        singleLine = true,
        shape = RectangleShape, // The TextFiled has rounded corners top left and right by default
        colors = TextFieldDefaults.textFieldColors(
            textColor = Color.Black,
            cursorColor = Color.Black,
            leadingIconColor = Color.Black,
            trailingIconColor = Color.Black,
            backgroundColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}