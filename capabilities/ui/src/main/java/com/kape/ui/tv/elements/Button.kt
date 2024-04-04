@file:OptIn(ExperimentalTvMaterial3Api::class, ExperimentalTvMaterial3Api::class)

package com.kape.ui.tv.elements

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import com.kape.ui.tv.text.PrimaryButtonText
import com.kape.ui.tv.text.SecondaryButtonText
import com.kape.ui.tv.text.TertiaryButtonText
import com.kape.ui.utils.LocalColors

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        shape = ButtonDefaults.shape(
            shape = RoundedCornerShape(12.dp),
        ),
        colors = ButtonDefaults.colors(
            containerColor = LocalColors.current.primaryContainer,
            contentColor = LocalColors.current.onSurfaceVariant,
            focusedContainerColor = LocalColors.current.primary,
            focusedContentColor = LocalColors.current.onPrimary,
        ),
        onClick = onClick,
    ) {
        PrimaryButtonText(
            content = text.uppercase(),
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    textAlign: TextAlign = TextAlign.Center,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        shape = ButtonDefaults.shape(
            shape = RoundedCornerShape(12.dp),
        ),
        colors = ButtonDefaults.colors(
            containerColor = LocalColors.current.background,
            contentColor = LocalColors.current.onSurfaceVariant,
            focusedContainerColor = LocalColors.current.primary,
            focusedContentColor = LocalColors.current.onPrimary,
        ),
        onClick = onClick,
    ) {
        SecondaryButtonText(
            content = text.uppercase(),
            textAlign = textAlign,
        )
    }
}

@Composable
fun TertiaryButton(
    text: String,
    textAlign: TextAlign = TextAlign.Center,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        shape = ButtonDefaults.shape(
            shape = RoundedCornerShape(12.dp),
        ),
        colors = ButtonDefaults.colors(
            containerColor = LocalColors.current.background,
            contentColor = LocalColors.current.onSurfaceVariant,
            focusedContainerColor = LocalColors.current.primary,
            focusedContentColor = LocalColors.current.onPrimary,
        ),
        onClick = onClick,
    ) {
        TertiaryButtonText(
            content = text.uppercase(),
            textAlign = textAlign,
        )
    }
}

@Composable
fun RoundIconButton(
    painterId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier,
        colors = ButtonDefaults.colors(
            containerColor = LocalColors.current.primaryContainer,
            contentColor = LocalColors.current.onSurface,
            focusedContainerColor = LocalColors.current.primary,
            focusedContentColor = LocalColors.current.onPrimary,
        ),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = painterId),
            contentDescription = null,
        )
    }
}

@Composable
fun TileButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit = { },
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        modifier = modifier,
        shape = ButtonDefaults.shape(
            shape = RoundedCornerShape(12.dp),
        ),
        colors = ButtonDefaults.colors(
            containerColor = LocalColors.current.onPrimaryContainer,
            contentColor = LocalColors.current.onSurface,
            focusedContainerColor = LocalColors.current.primary,
            focusedContentColor = LocalColors.current.onPrimaryContainer,
        ),
        onClick = onClick,
        onLongClick = onLongClick,
        content = content,
    )
}