package com.kape.ui.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.ui.R
import com.kape.ui.utils.LocalColors

// TODO: Implement colors properly
@Deprecated("to be removed")
@Composable
fun SearchBar(onSearchTextChanged: (text: String) -> Unit) {
    var searchText by remember { mutableStateOf("") }

    TextField(
        value = searchText,
        onValueChange = { value ->
            searchText = value
            onSearchTextChanged(value)
        },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                modifier = Modifier.padding(horizontal = 8.dp),
                imageVector = Icons.Default.Search,
                tint = LocalColors.current.onSurfaceVariant,
                contentDescription = stringResource(id = R.string.search),
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(id = R.string.clear),
                tint = LocalColors.current.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable {
                        searchText = ""
                        onSearchTextChanged("")
                    },
            )
        },
        singleLine = true,
        shape = RectangleShape, // The TextFiled has rounded corners top left and right by default
    )
}