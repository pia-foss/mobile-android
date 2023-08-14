package com.kape.ui.elements

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.kape.ui.R
import com.kape.ui.theme.Space
import com.kape.ui.theme.Square

// TODO: Implement colors properly
@OptIn(ExperimentalMaterial3Api::class)
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
        shape = RectangleShape // The TextFiled has rounded corners top left and right by default
    )
}