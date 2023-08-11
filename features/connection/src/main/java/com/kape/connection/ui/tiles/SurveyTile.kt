package com.kape.connection.ui.tiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kape.connection.R
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Space
import com.kape.ui.utils.LocalColors

@Composable
fun SurveyTile(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Space.NORMAL)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(id = R.drawable.ic_alert_green),
                contentDescription = null,
                tint = Color.Unspecified, modifier = Modifier.weight(0.1f)
            )
            Text(
                text = stringResource(id = R.string.survey_message),
                fontSize = FontSize.Normal,
                color = LocalColors.current.onSurface,
                modifier = Modifier
                    .padding(horizontal = Space.SMALL)
                    .weight(0.8f)
            )
            Icon(
                painter = painterResource(id = com.kape.ui.R.drawable.ic_close),
                contentDescription = null, tint = Color.Unspecified, modifier = Modifier
                    .weight(0.1f)
                    .clickable {

                    }
            )
        }
        Spacer(modifier = Modifier.height(Space.SMALL))
        Text(
            text = stringResource(id = R.string.survey_action),
            fontSize = FontSize.Normal,
            color = LocalColors.current.primary,
            modifier = Modifier
                .padding(horizontal = Space.VERY_BIG + Space.MINI)
                .clickable {
                    onClick.invoke()
                }
        )
    }
}