package com.kape.connection.ui.tiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kape.connection.R
import com.kape.uicomponents.theme.FontSize
import com.kape.uicomponents.theme.Grey20
import com.kape.uicomponents.theme.Space

@Composable
fun InAppMessageTile(content: String) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Space.NORMAL)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(id = R.drawable.ic_alert_green),
                contentDescription = stringResource(
                    id = com.kape.sidemenu.R.string.icon
                ), tint = Color.Unspecified, modifier = Modifier.align(Alignment.TopStart)
            )
            Icon(
                painter = painterResource(id = com.kape.uicomponents.R.drawable.ic_close),
                contentDescription = stringResource(
                    id = com.kape.sidemenu.R.string.icon
                ), tint = Color.Unspecified, modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable {

                    }
            )

        }
        Spacer(modifier = Modifier.height(Space.SMALL))
        Text(
            text = content,
            fontSize = FontSize.Normal,
            color = Grey20,
            modifier = Modifier.padding(horizontal = Space.MEDIUM)
        )
    }
}