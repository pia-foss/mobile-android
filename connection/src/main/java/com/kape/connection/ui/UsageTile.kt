package com.kape.connection.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kape.connection.R
import com.kape.connection.utils.UsageState
import com.kape.uicomponents.theme.FontSize
import com.kape.uicomponents.theme.Grey20
import com.kape.uicomponents.theme.Grey55
import com.kape.uicomponents.theme.Space

@Composable
fun UsageTile(state: UsageState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Space.NORMAL)
    ) {
        Text(
            text = stringResource(id = R.string.usage),
            color = Grey55,
            fontSize = FontSize.Small
        )
        Spacer(modifier = Modifier.height(Space.SMALL))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.download),
                    fontSize = FontSize.Tiny,
                    color = Grey20
                )
                Spacer(modifier = Modifier.height(Space.MINI))
                Text(
                    text = stringResource(id = R.string.usage_kb_to_format).format(state.download),
                    fontSize = FontSize.Normal,
                    color = Grey20
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.upload),
                    fontSize = FontSize.Tiny,
                    color = Grey20
                )
                Spacer(modifier = Modifier.height(Space.MINI))
                Text(
                    text = stringResource(id = R.string.usage_kb_to_format).format(state.upload),
                    fontSize = FontSize.Normal,
                    color = Grey20
                )
            }
        }
    }
}