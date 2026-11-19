package com.kape.settings.ui.elements

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.settings.domain.usecase.ReconnectPrompt

@Composable
fun ReconnectPromptDialog(
    prompt: ReconnectPrompt,
    onDone: () -> Unit = {},
    dimBackground: Boolean = false,
) {
    val isVisible by prompt.isVisible.collectAsStateWithLifecycle()
    if (!isVisible) return
    val dialog =
        @Composable {
            ReconnectDialog(
                onReconnect = {
                    prompt.onReconnect()
                    onDone()
                },
                onLater = {
                    prompt.onLater()
                    onDone()
                },
            )
        }
    if (dimBackground) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.6f)) { dialog() }
    } else {
        dialog()
    }
}