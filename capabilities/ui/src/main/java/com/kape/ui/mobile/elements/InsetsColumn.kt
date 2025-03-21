package com.kape.ui.mobile.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InsetsColumn(imageId: Int? = null, content: @Composable () -> Unit) {
    imageId?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .paint(painter = painterResource(imageId), contentScale = ContentScale.FillBounds)
                .semantics {
                    testTagsAsResourceId = true
                },
        ) {
            Spacer(
                Modifier.windowInsetsTopHeight(
                    WindowInsets.statusBars,
                ),
            )
            content()
        }
    } ?: run {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    testTagsAsResourceId = true
                },
        ) {
            Spacer(
                Modifier.windowInsetsTopHeight(
                    WindowInsets.statusBars,
                ),
            )
            content()
        }
    }
}