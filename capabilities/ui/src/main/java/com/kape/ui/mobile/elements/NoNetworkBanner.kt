package com.kape.ui.mobile.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kape.ui.utils.LocalColors

@Composable
fun NoNetworkBanner(noNetworkMessage: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalColors.current.error),
    ) {
        Text(
            text = noNetworkMessage,
            color = LocalColors.current.onError,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}