package com.kape.settings.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.settings.R
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Square
import com.kape.ui.utils.LocalColors

@Composable
fun SettingsItem(
    @DrawableRes iconId: Int,
    @StringRes titleId: Int,
    @StringRes subtitleId: Int? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = stringResource(
                id = R.string.icon
            ),
            tint = Color.Unspecified,
            modifier = Modifier.size(Square.ICON)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(id = titleId),
                fontSize = FontSize.Normal,
                color = LocalColors.current.onSurface
            )

            subtitleId?.let {
                Text(
                    text = stringResource(id = it),
                    fontSize = FontSize.Normal,
                    color = LocalColors.current.onSurfaceVariant
                )
            }
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow),
            contentDescription = null,
            tint = LocalColors.current.onSurface,
            modifier = Modifier.size(8.dp)
        )
    }
}