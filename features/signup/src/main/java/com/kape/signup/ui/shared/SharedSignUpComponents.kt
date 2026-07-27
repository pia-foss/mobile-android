package com.kape.signup.ui.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kape.ui.theme.PiaTypography
import com.kape.ui.theme.brandGreen
import com.kape.ui.utils.LocalColors

@Composable
internal fun CheckmarkText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            tint = LocalColors.current.brandGreen(),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.widthIn(12.dp))
        Text(
            text = text,
            color = LocalColors.current.onSurface,
            style = PiaTypography.body3,
            textAlign = TextAlign.Center,
        )
    }
}