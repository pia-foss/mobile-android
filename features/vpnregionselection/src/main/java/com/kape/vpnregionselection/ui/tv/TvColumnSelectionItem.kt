package com.kape.vpnregionselection.ui.tv

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.kape.ui.theme.PiaTypography
import com.kape.ui.utils.LocalColors

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvColumnSelectionItem(
    modifier: Modifier,
    onAllSelected: () -> Unit = { },
    onFavoriteSelected: () -> Unit = { },
    onSearchSelected: () -> Unit = { },
) {
    val colorScheme = LocalColors.current
    val selectedIndex = remember { mutableIntStateOf(0) }

    data class ButtonDetails(
        val title: String,
        val contentColor: MutableState<Color>,
        val containerColor: MutableState<Color>,
    )

    val buttons = listOf(
        ButtonDetails(
            title = stringResource(id = com.kape.ui.R.string.all),
            contentColor = remember { mutableStateOf(colorScheme.onSurface) },
            containerColor = remember { mutableStateOf(colorScheme.background) },
        ),
        ButtonDetails(
            title = stringResource(id = com.kape.ui.R.string.favorite),
            contentColor = remember { mutableStateOf(colorScheme.onSurface) },
            containerColor = remember { mutableStateOf(colorScheme.background) },
        ),
        ButtonDetails(
            title = stringResource(id = com.kape.ui.R.string.search),
            contentColor = remember { mutableStateOf(colorScheme.onSurface) },
            containerColor = remember { mutableStateOf(colorScheme.background) },
        ),
    )

    Column(
        modifier = modifier
            .onFocusChanged {
                buttons.forEachIndexed { index, buttonDetails ->
                    if (it.hasFocus.not() && selectedIndex.intValue == index) {
                        buttonDetails.containerColor.value = colorScheme.onPrimaryContainer
                        buttonDetails.contentColor.value = colorScheme.onSurface
                        return@forEachIndexed
                    }
                    buttonDetails.containerColor.value = colorScheme.background
                    buttonDetails.contentColor.value = colorScheme.onSurface
                }
            },
    ) {
        buttons.forEachIndexed { index, buttonDetails ->
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.hasFocus) {
                            selectedIndex.intValue = index
                            when (selectedIndex.intValue) {
                                0 -> onAllSelected()
                                1 -> onFavoriteSelected()
                                2 -> onSearchSelected()
                                else -> throw IllegalStateException("Unsupported TV Header State")
                            }
                        }
                    },
                shape = ButtonDefaults.shape(
                    shape = RoundedCornerShape(12.dp),
                ),

                colors = ButtonDefaults.colors(
                    containerColor = buttonDetails.containerColor.value,
                    contentColor = buttonDetails.contentColor.value,
                    focusedContainerColor = colorScheme.primary,
                    focusedContentColor = colorScheme.onPrimary,
                ),
                onClick = { },
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = buttonDetails.title,
                    textAlign = TextAlign.Start,
                    style = PiaTypography.button1,
                )
            }
        }
    }
}