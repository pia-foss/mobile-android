package com.kape.connection.ui.tiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kape.connection.R
import com.kape.connection.utils.UsageState
import com.kape.ui.elements.ConnectionTile
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Space
import com.kape.ui.utils.LocalColors

@Composable
fun UsageTile(state: UsageState) {
    ConnectionTile(labelId = R.string.usage) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.download),
                    fontSize = FontSize.Tiny,
                    color = LocalColors.current.onSurface,
                )
                Spacer(modifier = Modifier.height(Space.MINI))
                Text(
                    text = stringResource(id = R.string.usage_kb_to_format).format(state.download),
                    fontSize = FontSize.Normal,
                    color = LocalColors.current.onSurface,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.upload),
                    fontSize = FontSize.Tiny,
                    color = LocalColors.current.onSurface,
                )
                Spacer(modifier = Modifier.height(Space.MINI))
                Text(
                    text = stringResource(id = R.string.usage_kb_to_format).format(state.upload),
                    fontSize = FontSize.Normal,
                    color = LocalColors.current.onSurface,
                )
            }
        }
    }
}